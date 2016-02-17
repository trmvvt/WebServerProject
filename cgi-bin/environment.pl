#!/use/bin/perl
print "Content-type: text/html", "\n\n";
print "<HTML>", "\n";
print "<HEAD><TITLE>Environment Variables</TITLE></HEAD>", "\n";
print "<BODY><H1>Some Environment Variables</H1>", "\n";
print "<HR><PRE>", "\n";
print "SERVER NAME: ", $cgiEv{'SERVER_NAME'}, "<BR>", "\n";
print "SERVER PORT: ", $cgiEv{'SERVER_PORT'}, "<BR>", "\n";
print "SERVER PROTOCOL: ", $ENV{'SERVER_PROTOCOL'}, "<BR>", "\n";
print "CGI Revision: ", $ENV{'GATEWAY_INTERFACE'}, "<BR>", "\n";
print "REQUEST_METHOD ", $ENV{'REQUEST_METHOD'}, "<BR>", "\n";
print "HTTP_ACCEPT ", $ENV{'HTTP_ACCEPT'}, "<BR>", "\n";
print "<HR></PRE>", "\n";
print "</BODY></HTML>", "\n";