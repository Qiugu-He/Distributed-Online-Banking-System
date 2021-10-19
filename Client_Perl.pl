# ----------------------------------------------
# REMARK: CliPerl.pl
#	- Perl implemented client. Get request from stdin 
#       - then send and receive messgaes to/from server for 
#	  communivation.
# -----------------------------------------------

use strict;
use Socket;
use warnings;

my($sock);
# create the socket
socket($sock,AF_INET,SOCK_STREAM,(getprotobyname('tcp')))
   or die "Can't create a socket $!\n";
# connection to the server
my $remote = 'owl.cs.umanitoba.ca';
my $port = 13254;
my $iaddr = inet_aton($remote) or die "Unable to resolve hostname : $remote";
connect($sock, sockaddr_in($port, $iaddr))
		or die "Connection failed: $! \n";
print "Client starting. \n";
my $req = '';
my $exit = "E\n";
do
{	
	print "Please enter a request: \n";
	$req = <STDIN>;

	if(!($req eq $exit))
	{
		if($req =~ /[A-Z]\<\d*\>/ || $req =~ /[A-Z]\<\d*\,\d*\>/)
		{
			# send data to server
			send($sock, $req, 0)
				or die "sendo failed: !!";
			# receive a response from server
			print "Server echo: ";
			my $line = "";
			$line = <$sock>;
			print $line;
		}
		else
		{
			print "Invalid format. ";
		}
	}

}while(!($req eq $exit));

print "Client finished.\n";
 
close($sock);
exit(0);
