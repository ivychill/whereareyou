### Generated by rprotoc. DO NOT EDIT!
### <proto file: trackevent.proto>
# // See README.txt for information and build instructions.
# 
# package com.luyun.whereareyou.shared;
# 
# option java_package = "com.luyun.whereareyou.shared";
# option java_outer_classname = "TrackEventProtos";
# 
# message TrackEvent {
#   enum EventType {
#     START_TRACKING_REQ = 120; //from tracker_client(1) to tracker_server(2)
#     START_TRACKING_REP = 210; //from tracker_server(2) to tracker_client(1)
#     SEND_LOC_REQ = 320; //from trackee_server(3) to tracker_server(2)
#     SEND_LOC_REP = 230; //from tracker_server(2) to trackee_server(3)
#     QUERY_BY_ID_REQ = 321; //from trackee_server(3) to tracker_server(2)
#     QUERY_BY_ID_REP = 231; //from tracker_server(2) to trackee_server(3)
#     FWD_LOC_REQ = 211; //from tracker_server(2) to tracker_client(1)
#     FWD_LOC_REP = 121; //from tracker_client(1) to trackee_server(2)
#   }
#   required EventType type = 1 [default = START_TRACKING_REQ];
#   optional int32 id = 2;
#   
#   optional string tracker = 3;
#   optional string tracker_x = 4;
#   optional string tracker_y = 5;
#   optional string trackee = 6;
#   optional string trackee_x = 7;
#   optional string trackee_y = 8;
#   optional string trackee_desc = 9;
# }

require 'protobuf/message/message'
require 'protobuf/message/enum'
require 'protobuf/message/service'
require 'protobuf/message/extend'

module Com
  module Luyun
    module Whereareyou
      module Shared
        ::Protobuf::OPTIONS[:"java_package"] = "com.luyun.whereareyou.shared"
        ::Protobuf::OPTIONS[:"java_outer_classname"] = "TrackEventProtos"
        class TrackEvent < ::Protobuf::Message
          defined_in __FILE__
          class EventType < ::Protobuf::Enum
            defined_in __FILE__
            START_TRACKING_REQ = value(:START_TRACKING_REQ, 120)
            START_TRACKING_REP = value(:START_TRACKING_REP, 210)
            SEND_LOC_REQ = value(:SEND_LOC_REQ, 320)
            SEND_LOC_REP = value(:SEND_LOC_REP, 230)
            QUERY_BY_ID_REQ = value(:QUERY_BY_ID_REQ, 321)
            QUERY_BY_ID_REP = value(:QUERY_BY_ID_REP, 231)
            FWD_LOC_REQ = value(:FWD_LOC_REQ, 211)
            FWD_LOC_REP = value(:FWD_LOC_REP, 121)
          end
          required :EventType, :type, 1, :default => :START_TRACKING_REQ
          optional :int32, :id, 2
          optional :string, :tracker, 3
          optional :string, :tracker_x, 4
          optional :string, :tracker_y, 5
          optional :string, :trackee, 6
          optional :string, :trackee_x, 7
          optional :string, :trackee_y, 8
          optional :string, :trackee_desc, 9
        end
      end
    end
  end
end