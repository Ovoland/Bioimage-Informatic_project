maxEmails = 300;
// Get clipboard contents
emails = split(String.paste(), ";");
print( emails.length );
outlookExe = "C:\\Program Files (x86)\\Microsoft Office\\root\\Office16\\outlook.exe";

// Split every 300 emails
nEmails = Math.ceil( lengthOf(emails) / maxEmails );
if ( nEmails > 3) {
	doNext = getBoolean("This will create "+nEmails+" separate email messages!\n("+lengthOf(emails)+" recipients total)");
	if (!doNext) return;
}

for (i = 0; i < nEmails; i++) {
	part = Array.slice(emails,i*maxEmails,(i+1)*maxEmails);
	

	partString = String.join(part, ";");
	// Make a new email 
	
	exec(outlookExe, "/c" , "ipm.note" ,"/m", "biop-users@epfl.ch?bcc="+partString );

// Attempt top use EWA from firefox, but to no avail
//	exec("C:\\Program Files\\Mozilla Firefox\\firefox.exe", "--new-tab", "https://ewa.epfl.ch/owa/?path=/mail/action/compose&to="+partString);
//	print("C:\\Program Files\\Mozilla Firefox\\firefox.exe", "--new-tab", "https://ewa.epfl.ch/owa/?path=/mail/action/compose&to="+partString);

}