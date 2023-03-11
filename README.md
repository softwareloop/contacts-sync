# Contacts-sync

This is unfinished project intended to be a tool to synchronise LinkedIn contacts and Gmail addressbook.
Still it has been useful for me to experiment with OAuth and MongoDB.

The original design is explained below:

# Designing a new open-source tool to sync Gmail/LinkedIn contacts

(by Paolo Predonzani, originally posted on March 24, 2018)

Have you ever looked up an email or a phone number of one of your connections
on LinkedIn? I have, especially in these situation:

* I have met somebody at a conference or meeting. We have connected on
  LinkedIn and now I want to write an email to the person;
* A friend has moved to a new job. I have the friend's old contacts in my
  address book and now I want to update them with the new contacts 
  from LinkedIn.

I use Gmail's address book all the time and LinkedIn occasionally. It would be
nice if I could synchronise the contacts between the two. It would be nice
if there was a simple, open source tool to do that!

To address this problem, I'm starting a new project on Github called Contacts-sync: 
[https://github.com/softwareloop/contacts-sync](https://github.com/softwareloop/contacts-sync)

It has little code at the moment but in this blog post I'd like to share some
initial design thoughts.

## Contacts-sync design

Visually, contacts will be presented as cards.

Contacts-sync will present Gmail and LinkedIn cards next to
each other, in a way that makes it clear which contact on Gmail is associated
to which contact on LinkedIn.

![Contacts sync design: matched and unmatched contacts](https://github.com/softwareloop/contacts-sync/blob/master/images/contacts-sync-1.png?raw=true)

The association between Gmail and LinkedIn cards will be missing sometimes.
There will be Gmail cards without corresponding LinkedIn cards and vice versa.

There will also be cases where the cards for a person are present both on Gmail
and LinkedIn but for some reason (different name spelling or lack/mismatch
of other common attributes) the two cards are not associated.

Contacts-sync will help reconcile these situations.

![Contacts sync design: matching contacts](https://github.com/softwareloop/contacts-sync/blob/master/images/contacts-sync-2.png?raw=true)

Finally, when a pair of Gmail/LinkedIn cards is allocaited, there may be
different values for emails, phone numbers and other attributes. Contacts-sync
will highlight the situation to the user and help reconcile (sync) 
the differences.

![Contacts sync design: synch'ing a contact's details](https://github.com/softwareloop/contacts-sync/blob/master/images/contacts-sync-3.png?raw=true)

In general, contact details will be sync'ed from the LinkedIn card to the
Gmail card (and not the other way round), as a consequence of the fact that
LinkedIn contacts are public and read-only while the Gmail address book is 
personal and read/write.

## Tech stack

Contacts-sync will be a Java webapp to be installed locally or on a server.

The backend will include Spring Boot and optionally MongoDB for storage.

The frontend will includes Angular.js and [Material Design Lite](https://getmdl.io/).

This webapp will rely on Oauth2 to authorise the exchange of data with Gmail and LinkedIn.
Some setup will be required and will be documented when 
the project is more mature..

## Wait, wait, there are other tools that do that!

True. LinkedIn even has it's own 
[tool](https://www.linkedin.com/help/linkedin/answer/1278/syncing-contacts-from-other-address-books-and-sources?lang=en)
and mobile app to do it, but they sync in the wrong direction: from Gmail
to LinkedIn. I'd like them to work the other way round.

There are also other commercial tools to sync contacts, but are closed sources,
cannot be customised, and there can be privacy concerns about them.

I think creating an open source tool is the way to go. I also think that
contact sync'ing is quite a generic requirement across different
address books, social networks and intranet services. With some tweaking and
extension, Contacts-sync can be the foundation to address those diverse situations.

I have also looked for similar open source tools, specifically in Java, but
found none. If you're aware of any, let me know and I'll be glad to reference
them.

## In summary

This blog post is just the announcement of this new project, which will
hopefully be useful to a lot of people. 
