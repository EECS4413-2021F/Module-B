#!/usr/bin/env perl
# From: http://www.cs.toronto.edu/~penny/teaching/csc309-01f/lectures/40/cgi-bin.zip
use strict;

print "Content-type: text/html\n\n";
print <<EOT1;
<html>
<head>
</head>
<body bgcolor=ffffff>
<h3>Hello!!</h3>
This web page was returned by a CGI script
<table border>
EOT1
#Dynamically generate a table, 6 rows, 4 columns
#We could have read the QUERY_STRING environment variable and
#obtained the dimensions from  there!!
my $i; my $j;
for($i=0;$i<6;$i++){
	print "<tr>";
	for($j=0;$j<4;$j++){
		print "<td>($i,$j)</td>";
	}
	print "</tr>\n";
}
print <<EOT2;
</table>
</ul>
</body>
</html>
EOT2
