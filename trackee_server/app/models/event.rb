class Event
  include Mongoid::Document
  field :tracker, :type => String
  field :trackee, :type => String
  field :tracker_x, :type => String
  field :tracker_y, :type => String
  field :trackee_x, :type => String
  field :trackee_y, :type => String
  field :trackee_desc, :type => String
end
