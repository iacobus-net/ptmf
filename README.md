Power Transport Multicast Framework (PTMF)
====

# You can find more information on [ptmf.iacobus.net](http://ptmf.iacobus.net)

Power Transport Multicast Framework (PTMF) is a FREE framework for developing portable Java reliable, semireliable and unreliable multicast applications.


# Origin
PTMF is initialy based on a novel reliable multicast transport protocol (level 4 of the OSI stack) developed at Universidad de Sevilla. PTMF has been implemented with a herarchical design based on local groups that help minimizing control flow and permit the grow of the numbers of participants to a high level.

# Target
Provide a usefull framework to easy develop multicast applications.
PTMF is been designed to be used to implement Scada Multicast on SmartGrids.
At present test applications are using IEC 60870-5-104 protocol. 
New protocol IEC 61850 could be used too.

# mFtp - Multicast ftp
Added MFTP. An Multicast Ftp tool
Use: java -jar mftp1.2.jar

# mChat - Multicast chat
Added MChat. An Multicast Chat tool
Use: java -jar mchatv1.2.jar

# Multicast tools
Added mutils. A simple multicast ping tool for send and receive multicast packets to test multicast communications on your network.

Use: java -cp mutil2.0.jar iacobus.mutil.mPingSender
Use: java -cp mutil2.0.jar iacobus.mutil.mPingReceiver


# Licence
Power Transport Multicast Framework (v2.0) is now available under Apache 2.0
