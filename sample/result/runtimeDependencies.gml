graph [
	node [
		id 1
		blueprintsId "#9:-2"
		references "[Process2ServiceReference]"
		services "[process1_service_ep]"
		projectFile "c:\WorkspacesGitlab\oracle.soa\sample\ProjectX\ProjectX.jpr"
		projectName "ProjectX"
	]
	node [
		id 2
		blueprintsId "#9:-3"
		references "[Process3ServiceReference]"
		services "[process2_service_ep]"
		projectFile "c:\WorkspacesGitlab\oracle.soa\sample\ProjectY\ProjectY.jpr"
		projectName "ProjectY"
	]
	node [
		id 3
		blueprintsId "#9:-4"
		references "[Process1ServiceReference]"
		services "[process3_service_ep]"
		projectFile "c:\WorkspacesGitlab\oracle.soa\sample\ProjectZ\ProjectZ.jpr"
		projectName "ProjectZ"
	]
	edge [
		source 1
		target 2
		label "REFERENCES"
		blueprintsId "#11:-5"
		description "Process2ServiceReference"
		URL "Process2ServiceReference"
	]
	edge [
		source 2
		target 3
		label "REFERENCES"
		blueprintsId "#11:-6"
		description "Process3ServiceReference"
		URL "Process3ServiceReference"
	]
	edge [
		source 3
		target 1
		label "REFERENCES"
		blueprintsId "#11:-7"
		description "Process1ServiceReference"
		URL "Process1ServiceReference"
	]
]
