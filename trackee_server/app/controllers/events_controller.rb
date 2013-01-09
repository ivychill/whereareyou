require "trackevent.pb"
require 'ffi-rzmq'

class EventsController < ApplicationController
  
  def index
      puts 'index'
  end

  def show
    eid = params[:id]
    puts eid
  end

  def update
    eid = params[:id]
    puts eid
    trackee_x = params[:trackee_x]
      #    puts trackee_x
    trackee_y = params[:trackee_y]
      #    puts trackee_y
    
    @event = params[:event]
    
    te = Com::Luyun::Whereareyou::Shared::TrackEvent.new
    #te.tracker = @event.tracker
    #te.trackee = @event.trackee
    te.id = Integer(eid)
    #te.id = 19752012
    te.trackee_x = params[:trackee_x]
    te.trackee_y = params[:trackee_y]
    te.trackee_desc = params[:trackee_desc]
    te.type = Com::Luyun::Whereareyou::Shared::TrackEvent::EventType::SEND_LOC_REQ
    serialized_te = te.serialize_to_string
    
      #    puts "dddd"
    zcontext = ZMQ::Context.new(1)
      #puts "eeeee"
      #puts serialized_te
    outbound = zcontext.socket ZMQ::DEALER
      #    puts "ggggg"
    outbound.setsockopt ZMQ::IDENTITY, "TRACKEE_WEB_SVR"
    outbound.connect("tcp://172.16.0.105:8007")
      #    puts "ffff"

    outbound.send_string serialized_te
      #    puts "222222"
    result_msg = ""
    outbound.recv_string result_msg
    result_te = Com::Luyun::Whereareyou::Shared::TrackEvent.new
    result_te.parse_from_string result_msg
    puts result_te.tracker
    puts result_te.trackee
    
    outbound.close
    zcontext.terminate
    
      #puts serialized_te
    
    respond_to do |format|
      format.json { render json: @event }
    end
  end

end
