# Read about factories at https://github.com/thoughtbot/factory_girl

FactoryGirl.define do
  factory :event do
    tracker "MyString"
    trackee "MyString"
    tracker_x "MyString"
    tracker_y "MyString"
    trackee_x "MyString"
    trackee_y "MyString"
  end
end
