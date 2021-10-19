# ----------------------------------------------
# STUDENT NAME: Qiugu He
# STUDENT NUMBER: 7768346
# COURSE NAME: COMP3010 
# ASSIGNMENT: A2
# REMARK: CliPy.py
#	- python implemented client. Get request from stdin
#     then send and receive messgaes to/from server for 
#	  communivation.
# -----------------------------------------------

import socket
import sys
from time import sleep
import re

#function to recv the data from server
def recvMsg():
	text = ""
	while True:
		data = sock.recv(1)
		text += data
		if data == "\n":
			break
	return text

# Create socket
try:
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
except socket.error:
	print('Failed to create socket')
	sys.exit()

# Connect to the server
sock.connect((socket.gethostbyname('owl.cs.umanitoba.ca'), 13254))
sock.settimeout(2)
print("Client starting. ")

# readin request and communication with server
close = 0
exit ='E\n'
while(close == 0):
	print ("Please enter a request: ")
	request = sys.stdin.readline()
	if request != exit:
		if re.match("([A-Z])<(\d+)>", request) or re.match("([A-Z])<(\d+),(\d+)>", request):
			# send request to server
			sock.sendall(request)
			# receive response
			response = recvMsg()
			print("Server ecoh:" + response)
		else:
			print("Invalid operation foramt.")
	else:
		close = 1

print("Client finished.")
sock.close()

