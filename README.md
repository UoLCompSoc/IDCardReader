IDCardReader
============

What's This?
------------
A very simple Android app which can read the student ID and expiration date from a University NFC ID card.

Isn't that Breaking some rules?
-------------------------------
No; the ID cards contain a mix of encrypted and unencrypted data. This app makes no attempt to touch the encrypted data in any way; it merely reads the unencrypted (i.e. publically accessible) data from the card. There's nothing stored unencrypted that you couldn't read from the front of the card anyway; there's actually more unencrypted data on the front than stored inside the card.

How do I use it?
----------------
You'll need to install Android Studio and import the project yourself. Minimum SDK API level is 18 (Android 4.3). Tested on a Galaxy SIII; you'll obviously need a phone with NFC support.

This is not intended as a production app, merely a demo that you can use to play around with the publicly available data you have in your pocket on a day-to-day basis.

Once you've got it running, just hold the card up to your phone; if you have more than one app which handles NFC cards, you'll need to select this app (NFCPlayground) and keep the card held in place.

The Communication Methods are Illegible!
----------------------------------------
True; for "security reasons" the type of card the University uses (Mifare DESFire v1) have no public command list available, so the commands you see are just the raw hex sent to and received from the card. We don't really want to go into more detail than that; we've not seen the actual command list (for which you need to sign an NDA) and so the commands you see are mainly a product of luck rather than judgement.

