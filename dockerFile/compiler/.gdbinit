define hook-stop
	printf "@sl@\n"
	info locals
	printf "@el_sbt@\n"
	backtrace -16
	printf "@ebt_sib@\n"
end

#set confirm off

