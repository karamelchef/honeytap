sudo touch solo.rb
sudo chmod 777 solo.rb
cat > solo.rb <<-'END_OF_FILE'
file_cache_path "/tmp/chef-solo"
cookbook_path [%cookbooks_path%]
END_OF_FILE