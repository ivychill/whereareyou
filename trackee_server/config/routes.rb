Trackeeserver::Application.routes.draw do

  resources :events
  match "/t" => redirect("/events")
  match "/t/:id" => redirect("/events/%{id}")
  
  match "/d/:id" => "dest#show"
  authenticated :user do
    root :to => 'home#index'
      
  end
    
  root :to => "home#index"
  devise_for :users
  resources :users, :only => [:show, :index]
end
