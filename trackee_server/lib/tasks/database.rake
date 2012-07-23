namespace :db do
    namespace :model do
	task :export, [ :model, :filename ] => :environment do |t, args|
	  model = args[:model].constantize
	  filename = args[:filename]
	  objects = model.find(:all)
	  File.open(File.join(Rails.root, filename), "w") do |f|
	    objects.each do |object|
	      f.write(object.to_json)
	      f.write("\r\n")
	    end
	  end
	end
	task :import, [ :model, :filename ] => :environment do |t, args|
	  model = args[:model].constantize
	  model.destroy_all
	  filename = args[:filename]
	  File.foreach(File.join(Rails.root, filename)) do |line|
	      next if line.blank?
	      object = model.new.from_json line.strip
	      object.save!
	  end
	end
    end

    def collections
    [
      { model: "Road", table: "roads" }
    ]
    end
    task :export => :environment do
        collections.each do |collection|
          table = collection[:table]
          model = collection[:model]
          dir = "db/seed/" + Rails.env
          filename = dir + "/" + table + ".json"
          Dir.mkdir dir unless (Dir.exists? dir)
          Rake::Task["db:model:export"].execute({:model=> model, :filename=>filename})
        end
    end
    task :import => :environment do
        collections.each do |collection|
          table = collection[:table]
          model = collection[:model]
          dir = "db/seed/" + Rails.env
          filename = dir + "/" + table + ".json"
          Rake::Task["db:model:import"].execute({model: model, filename: filename})
        end
    end
end