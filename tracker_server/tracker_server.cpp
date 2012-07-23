//
//

#include "tracker_server.h"
#include <unistd.h>
#include <time.h>
using namespace std;

void TrackEventHelper::parseFromWire(std::string& msg, zmq::socket_t& socket)
{
	com::luyun::whereareyou::shared::TrackEvent *pTrackEventOnWire = new com::luyun::whereareyou::shared::TrackEvent;
	//this pointer should be released in this function
	
	pTrackEventOnWire->ParseFromString(msg);
	std::cout <<"event on receive: "<< std::endl<< pTrackEventOnWire->DebugString();
	switch (pTrackEventOnWire->type()) 
	{
		case com::luyun::whereareyou::shared::TrackEvent::START_TRACKING_REQ:
		{
			//get a request from tracker_client
			//store track event into map
			//allocate event id
			if (pTrackEventOnWire->has_tracker() == false || pTrackEventOnWire->has_trackee() == false) 
			{
				std::cerr << "START_TRACKING_REQ must carry tracker and trackee." << std::endl;
				delete pTrackEventOnWire;
				return;
			}
			std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator it;
			it = findOrCreate(pTrackEventOnWire);
			//very similar to a rails controller post/create: return the same record just created.
			it->second->set_type(com::luyun::whereareyou::shared::TrackEvent::START_TRACKING_REP);
			std::string strOnWire;
			it->second->SerializeToString(&strOnWire);
			std::cout << "to client" << it->second->DebugString();
			
			s_sendmore(socket, it->second->tracker());
			s_send(socket, strOnWire);
			break;
		}
		case com::luyun::whereareyou::shared::TrackEvent::SEND_LOC_REQ:
		{
			//get the location update from trackee_server
			//find a matched track event and update the location
			//std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator it;
			//it = mapTrackEventOnAir.find(genKey(pTrackEvent))->second;
			//com::luyun::whereareyou::shared::TrackEvent* pTrackEventOnWire = it->second;
			if (pTrackEventOnWire->has_id() == false) 
			{
				std::cerr << "SEND_LOC_REQ must carry event id." << std::endl;
				delete pTrackEventOnWire;
				return;
			}
			com::luyun::whereareyou::shared::TrackEvent* pTrackEventOnAir = NULL;
			std::map<int, com::luyun::whereareyou::shared::TrackEvent*>::iterator it;
			it = mapIndexOfTrackEventOnAir.find(pTrackEventOnWire->id());
			if (it != mapIndexOfTrackEventOnAir.end())
			{
				pTrackEventOnAir = it->second;
				pTrackEventOnAir->set_trackee_x(pTrackEventOnWire->trackee_x());
				pTrackEventOnAir->set_trackee_y(pTrackEventOnWire->trackee_y());
				
				//send back track event to client
				std::string strOnWire;
				pTrackEventOnAir->set_type(com::luyun::whereareyou::shared::TrackEvent::FWD_LOC_REQ);
				pTrackEventOnAir->SerializeToString(&strOnWire);
				std::cout << "to tracker client" << std::endl << pTrackEventOnAir->DebugString();
				s_sendmore(socket, pTrackEventOnAir->tracker());
				s_send(socket, strOnWire);
				
				//send back track event to trackee server
				pTrackEventOnAir->set_type(com::luyun::whereareyou::shared::TrackEvent::SEND_LOC_REP);
				pTrackEventOnAir->SerializeToString(&strOnWire);
				std::cout << "to trackee server" << std::endl << pTrackEventOnAir->DebugString();
				s_sendmore(socket, TRACKEE_WEB_SVR);
				s_send(socket, strOnWire);
			}
			else
			{
				//for some reasons such as memory garbage mechanism triggered, the event stored in map was thrown into garbage.
				//cannot just discard the message, for trackee_server is expecting for a reply
				pTrackEventOnWire->set_type(com::luyun::whereareyou::shared::TrackEvent::SEND_LOC_REP);
				std::string strOnWire;
				pTrackEventOnWire->SerializeToString(&strOnWire);
				std::cout <<"event to trackee server "<< std::endl<< pTrackEventOnWire->DebugString();
				s_sendmore(socket, TRACKEE_WEB_SVR);
				s_send(socket, strOnWire);
			}
			break;
		}
		case com::luyun::whereareyou::shared::TrackEvent::QUERY_BY_ID_REQ:
		{
			//get the stored tracker and trackee details by trackee_server
			if (pTrackEventOnWire->has_id() == false) 
			{
				std::cerr << "QUERY_BY_ID_REQ must carry Id." << std::endl;
				delete pTrackEventOnWire;
				return;
			}
			com::luyun::whereareyou::shared::TrackEvent* pTrackEventOnAir;
			std::map<int, com::luyun::whereareyou::shared::TrackEvent*>::iterator it;
			it = mapIndexOfTrackEventOnAir.find(pTrackEventOnWire->id());
			if (it != mapIndexOfTrackEventOnAir.end())
			{
				pTrackEventOnAir = it->second;
				std::string strOnWire;
				pTrackEventOnAir->set_type(com::luyun::whereareyou::shared::TrackEvent::QUERY_BY_ID_REP);
				pTrackEventOnAir->SerializeToString(&strOnWire);
				s_sendmore(socket, TRACKEE_WEB_SVR);
				s_send(socket, strOnWire);
			}
			else
			{
				//for some reasons such as memory garbage mechanism triggered, the event was thrown into garbage.
				pTrackEventOnWire->set_type(com::luyun::whereareyou::shared::TrackEvent::SEND_LOC_REP);
				std::string strOnWire;
				pTrackEventOnWire->SerializeToString(&strOnWire);
				std::cout <<"event to trackee server: "<< std::endl<< pTrackEventOnWire->DebugString();
				s_sendmore(socket, TRACKEE_WEB_SVR);
				s_send(socket, strOnWire);
			}
			break;
		}
		case com::luyun::whereareyou::shared::TrackEvent::SEND_LOC_REP:
		case com::luyun::whereareyou::shared::TrackEvent::FWD_LOC_REQ:
		case com::luyun::whereareyou::shared::TrackEvent::FWD_LOC_REP:
		case com::luyun::whereareyou::shared::TrackEvent::START_TRACKING_REP: 
		case com::luyun::whereareyou::shared::TrackEvent::QUERY_BY_ID_REP: 
		default:
			std::cerr << "wrong command:" << pTrackEventOnWire->type() << std::endl;
			break;
	}
	delete pTrackEventOnWire;
}

void TrackEventHelper::clearGarbage()
{
	if (mapTrackEventOnAir.size() < MAX_TRACKING_EVENT_ON_AIR_THRESHOLD) return;
	for (int i=0; i<MAX_TRACKING_EVENT_ON_AIR/2; i++)
	{
		com::luyun::whereareyou::shared::TrackEvent* pTrackEventOnAir = queueIndexOfTrackEventOnAir.front();
		
		std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator it;
		it = mapTrackEventOnAir.find(genKey(pTrackEventOnAir));
		mapTrackEventOnAir.erase(it);
		
		std::map<int, com::luyun::whereareyou::shared::TrackEvent*>::iterator it2;
		it2 = mapIndexOfTrackEventOnAir.find(pTrackEventOnAir->id());
		mapIndexOfTrackEventOnAir.erase(it2);
		
		delete pTrackEventOnAir;
		queueIndexOfTrackEventOnAir.pop();
	}
}

std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator
	TrackEventHelper::findOrCreate(com::luyun::whereareyou::shared::TrackEvent* pTrackEvent)
{
	//clear garbage
	clearGarbage();
	
	int iEventId = rand() % RANDOMSEED + RANDOMSEED;
	iEventId = 19752012;
	com::luyun::whereareyou::shared::TrackEvent* pTrackEventOnWire = new com::luyun::whereareyou::shared::TrackEvent;
	//this pointer will be release in the destructor
	
	//clone pTrackEvent
	pTrackEventOnWire->set_tracker(pTrackEvent->tracker());
	pTrackEventOnWire->set_trackee(pTrackEvent->trackee());
	pTrackEventOnWire->set_id(iEventId);
	pair<std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator, bool> ret;
	ret = mapTrackEventOnAir.insert(pair<std::string, com::luyun::whereareyou::shared::TrackEvent *>(genKey(pTrackEventOnWire), 
			pTrackEventOnWire));
	if (ret.second == true) // that means a new pair<key, TrackEvent pointer> was successfully created.
	{
		//then queue for indexes to prevent memory leak or crash
		queueIndexOfTrackEventOnAir.push(pTrackEventOnWire);
	}
	pair<std::map<int, com::luyun::whereareyou::shared::TrackEvent*>::iterator, bool> indexRet;
	//then loop to create a valid index
	while (true)
	{
		indexRet = mapIndexOfTrackEventOnAir.insert(pair<int, com::luyun::whereareyou::shared::TrackEvent *>(iEventId, 
				pTrackEventOnWire));
		if (indexRet.second == true) break; //no conflict occured for generated key (event id)
		iEventId = iEventId + rand() % RANDOMSEED + 1;
		pTrackEventOnWire->set_id(iEventId);
		ret.first->second->set_id(iEventId);
	}
	return ret.first;
}

int main() {
    TrackEventHelper *newTrackerEventHelper = new TrackEventHelper();
    zmq::context_t context (1);
    zmq::socket_t socket (context, ZMQ_ROUTER);
    socket.bind ("tcp://*:8002");
    
    //loop for waiting connection from tracker_client or trackee_server
    while (true) {
        //address of peer we don't care, because it is included in the message

        // popup client address
        std::string address = s_recv (socket);
        
        //fetch track event
        std::string request = s_recv (socket);
        newTrackerEventHelper->parseFromWire(request, socket);
                
    }
    //check if a new request from client or trackee_server
    //trackee_server's event_type should always be equal to SEND_LOC_REQ
    //tracker_client
    delete newTrackerEventHelper;
}
