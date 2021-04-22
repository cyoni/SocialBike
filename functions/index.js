
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
    accountActivated: true,
  });
});


function verifyUser(userPrivateKey) {
    const account = (async (resolve, reject) => {
        const snapshot = await admin.database().ref('users').child(userPrivateKey).once('value');
        if (snapshot.exists() && snapshot.child('accountActivated').val() === true) {
            return resolve({
                publicKey: snapshot.child('user_public_key').val(),
            });
        }
        else
            return null;
    })
    return new Promise(account);
}


async function isNicknameTaken(inputNickname) {
    const nickname = inputNickname.toLowerCase();
    const result = await admin.database().ref('nicknames').child(nickname).once('value');
    return (result.exists());
}


function isNicknameValid(nickname) {
    return !(nickname.length > 20 || nickname.toLowerCase() === "private" || nickname.includes("$") ||
        nickname.includes(".") || nickname.includes("/") ||
        nickname.includes("[") || nickname.includes("]") ||
        nickname.includes("\\"))
}


async function setNewNickname(publicKey, newNickname) {
    const a = (admin.database().ref('nicknames').child(newNickname.toLowerCase()).set(publicKey));
    const b = (admin.database().ref('public').child(publicKey).child('profile').child('nickname').set(newNickname));
    await a
    await b
}


function getNickname(publicKey) {
    return admin.database().ref('public').child(publicKey).child('profile').child('nickname').once('value').then(res => {
        let user_nickname = res.val();
        if (user_nickname === null)
            user_nickname = "No Nickname";
        return user_nickname;
    })
}


function removeOldNickname(oldNickname) {
    return admin.database().ref('nicknames').child(oldNickname).remove();
}


exports.updateProfile = functions.https.onCall(async (data, context) => {
    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "[AUTH_FAILED]";

    const publicKey = account.publicKey

    const country = context.params.country;
    const city = context.params.city;
    const gender = context.params.gender;
    const age = context.params.age;
 
    const a = (admin.database().ref('public').child(publicKey).child('profile').child('country').set(country));
    const b = (admin.database().ref('public').child(publicKey).child('profile').child('city').set(city));
    const c = (admin.database().ref('public').child(publicKey).child('profile').child('gender').set(gender));
    const d = (admin.database().ref('public').child(publicKey).child('profile').child('age').set(age));

    await a, b, c, d;
    return "OK"
})

exports.updateNickname = functions.https.onCall(async (data, context) => {
    const privateKey = context.auth.uid;
    var inputNickname = data.nickname.trim();
    console.log("1");
    const account = await verifyUser(privateKey);

    console.log("2");
    if (account === null)
        return "[AUTH_FAILED]";

    const validNickname = isNicknameValid(inputNickname);

    if (!validNickname)
        return "[INVALID_NICKNAME]"

    const takenNickname = await isNicknameTaken(inputNickname);

    if (takenNickname)
        return "[NICKNAME_TAKEN]"

        console.log("3");
    const oldNickname = await getNickname(account.publicKey);
    if (oldNickname !== "")
        removeOldNickname(oldNickname.toLowerCase());
        console.log("4");

    await setNewNickname(account.publicKey, inputNickname);
    console.log("5");
    return inputNickname;
});
