# Place all the behaviors and hooks related to the matching controller here.
# All this logic will automatically be available in application.js.
# You can use CoffeeScript in this file: http://jashkenas.github.com/coffee-script/
addrs = null
startIndex = 0
resolved_pois = 0
to_be_resolved_pois = []

$(document).ready ->
	initMap()
	#post
	#allroads = get_all_roads()
