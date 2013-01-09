class DestController < ApplicationController
    def show
        puts 'call dest show'
        eid = params[:id]
        puts eid
    end
end
