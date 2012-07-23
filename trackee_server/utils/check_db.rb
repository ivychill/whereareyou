require File.expand_path("../util_helper", __FILE__)

$unresolved_pois = []

def db_status
	total_roads = Road.all.size
	total_pois = 0
	solved_pois = 0
	Road.all.each do |road|
		road.pois.each do |poi|
			total_pois = total_pois+1
			if poi.X && poi.Y
				solved_pois=solved_pois+1 
			else
				unresolved_poi = {:road_name => road.name, :road_id => road._id, :ref => poi.ref, :ref_type => poi.ref_type}
				$unresolved_pois.push unresolved_poi
			end
		end
	end
	{:total_roads => total_roads, :total_pois => total_pois, :solved_pois => solved_pois}
end

def clear_db
	Road.all.each do |road|
		road.pois.each do |poi|
			poi.destroy if !poi.ref
			drop_duplicated_records(road.pois, poi)
		end
	end
end

def drop_duplicated_records(pois, poi)
	pois.each do |xx|
		xx.destroy if xx.ref.match(poi.ref) && xx._id != poi._id
	end
end

clear_db

puts db_status
puts "unresolved pois"
$unresolved_pois.each do |poi|
	puts "road_name: "+poi[:road_name]+",road_id:"+poi[:road_id].to_s+",ref:"+poi[:ref] #+",ref_type: "+poi[:ref_type]
end