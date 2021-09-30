#!/usr/bin/env perl
# From: http://www.cs.toronto.edu/~penny/teaching/csc309-01f/lectures/40/cgi-bin.zip

print "Content-type: text/html\n\n";

print "<pre style='font: bolder 24pt'>\n";
print "Environment\n";

@keys = keys %ENV;
@values = values %ENV;

while (@keys) { 
  print pop(@keys), '=', pop(@values), "\n";
}

print "STDIN\n";
#while(<>){
#  print;
#}

print "</pre>\n";

