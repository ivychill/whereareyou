ENV["RAILS_ENV"] ||= 'development'
require File.expand_path("../../config/environment", __FILE__)

# Requires supporting ruby files with custom matchers and macros, etc,
# in spec/support/ and its subdirectories.
Dir[Rails.root.join("utils/support/**/*.rb")].each {|f| require f}

