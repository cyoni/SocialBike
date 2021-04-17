const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.addUserToDB = functions.auth.user().onCreate( (event) => {
  const timestamp = Date.now();
  const publicKey = admin.database().ref("public").push().key;
  return admin.database().ref("users/" + event.uid).set({
    email: event.email,
    joinTimestamp: timestamp,
    userPublicKey: publicKey,
  });
});
