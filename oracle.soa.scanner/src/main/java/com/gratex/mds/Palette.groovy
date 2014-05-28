package com.gratex.mds

class Palette {

	private static final INSTANCE = new Palette()
	static getInstance(){ return INSTANCE }

	def stack = [
		"#FFECEC",
		"#FFEEFB",
		"#FFECF5",
		"#FFEEFD",
		"#FDF2FF",
		"#FAECFF",
		"#F1ECFF",
		"#FFECFF",
		"#F4D2F4",
		"#F9EEFF",
		"#F5EEFD",
		"#EFEDFC",
		"#EAF1FB",
		"#DBF0F7",
		"#EEEEFF",
		"#ECF4FF",
		"#F9FDFF",
		"#E6FCFF",
		"#F2FFFE",
		"#CFFEF0",
		"#EAFFEF",
		"#E3FBE9",
		"#F3F8F4",
		"#F1FEED",
		"#E7FFDF",
		"#F2FFEA",
		"#FFFFE3",
		"#FCFCE9",
		"#FF9797",
		"#FF97E8",
		"#FF97CB",
		"#FE98F1",
		"#ED9EFE",
		"#E29BFD",
		"#B89AFE",
		"#FF4AFF",
		"#DD75DD",
		"#C269FE",
		"#AE70ED",
		"#A095EE",
		"#7BA7E1",
		"#57BCD9",
		"#8C8CFF",
		"#99C7FF",
		"#99E0FF",
		"#63E9FC",
		"#74FEF8",
		"#62FDCE",
		"#72FE95",
		"#7CEB98",
		"#93BF96",
		"#99FD77",
		"#52FF20",
		"#95FF4F",
		"#FFFFAA",
		"#EDEF85"
	]

	Palette(){
		Collections.shuffle(stack, new Random())
	}

	def idMap = [:]

	def getColor(id) {
		def c = idMap[id]
		if(c == null) {
			c = stack.pop()
			idMap[id] = c
		}
		c
	}

}
