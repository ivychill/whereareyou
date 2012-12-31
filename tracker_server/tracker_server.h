//
//

#include "zhelpers.hpp"
#include "my_log.h"
#include "proto/trackevent.pb.h"
#include <queue>

#define LOG_ERR(inf) LOG4CPLUS_ERROR(log, inf)
#define LOG_INF(inf) LOG4CPLUS_INFO(log, inf)
#define LOG_DBG(inf) LOG4CPLUS_DEBUG(log, inf)

const int RANDOMSEED = 19752012;

const int MAX_TRACKING_EVENT_ON_AIR = 1024*500;
const int MAX_TRACKING_EVENT_ON_AIR_THRESHOLD = MAX_TRACKING_EVENT_ON_AIR*0.9; //when numbers of tracking events achieves MAX_TRACKING_EVENT_ON_AIR * CAPS, clearGarbage will be triggered.

const char *TRACKEE_WEB_SVR = "TRACKEE_WEB_SVR";

class TrackEventHelper
{
    std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*> mapTrackEventOnAir; //default index by tracker+trackee
    std::map<int, com::luyun::whereareyou::shared::TrackEvent*> mapIndexOfTrackEventOnAir; // index by the event id
    //std::map<timeval, com::luyun::whereareyou::shared::TrackEvent*> mapIndexOfTrackEventOnAirByTime; //index by the time
    std::queue<com::luyun::whereareyou::shared::TrackEvent*> queueIndexOfTrackEventOnAir; //index by the time
    
  public:
    TrackEventHelper ()
    {
    }
    ~TrackEventHelper ()
    {
    	//std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator it;
    	//for ( it=mapTrackEventOnAir.begin() ; it != mapTrackEventOnAir.end(); it++ )
    	//	if (it->second != NULL) delete it->second;
    	while (!queueIndexOfTrackEventOnAir.empty()) 
    	{
    		com::luyun::whereareyou::shared::TrackEvent* pTrackEvent = queueIndexOfTrackEventOnAir.front();
    		delete pTrackEvent;
    		queueIndexOfTrackEventOnAir.pop();
    	}
    	mapTrackEventOnAir.erase(mapTrackEventOnAir.begin(), mapTrackEventOnAir.end());
    	mapIndexOfTrackEventOnAir.erase(mapIndexOfTrackEventOnAir.begin(), mapIndexOfTrackEventOnAir.end());
    }
    
    void parseFromWire(std::string& msg, zmq::socket_t& socket);
    std::map<std::string, com::luyun::whereareyou::shared::TrackEvent*>::iterator
    	findOrCreate(com::luyun::whereareyou::shared::TrackEvent& pTrackEvent);
    
    std::string genKey(com::luyun::whereareyou::shared::TrackEvent* pTrackEvent)
    {
    	std::string strKey="";
    	strKey.append(pTrackEvent->tracker());
    	strKey.append(pTrackEvent->trackee());
    	//std::stringstream strTemp;
    	//strTemp << pTrackEvent->id();
    	//strKey.append(strTemp.str());
    	return strKey;
    }
    
    void clearGarbage();
};



