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
    const account = ((resolve, reject) => {
        return admin.database().ref('users').child(userPrivateKey).once('value').then(snapshot => {
            if (snapshot.exists() && snapshot.child('accountActivated').val() === true) {
                return resolve({
                    publicKey: snapshot.child('userPublicKey').val(),
                })
            } else
                return null;
        })
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


function setNewNickname(publicKey, newNickname) {
    const a = await (admin.database().ref('nicknames').child(newNickname.toLowerCase()).set(publicKey));
    const b = await (admin.database().ref('user_public').child(publicKey).child('profile').child('nickname').set(newNickname));
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

exports.updateNickname = functions.https.onCall(async (data, context) => {
    const privateKey = context.auth.uid;
    var inputNickname = data.nickname.trim();

    const account = await verifyUser(privateKey);

    if (account === null)
        return "[AUTH_FAILED]";

    const validNickname = isNicknameValid(inputNickname);

    if (!validNickname)
        return "[INVALID_NICKNAME]"

    const takenNickname = await isNicknameTaken(inputNickname);

    if (takenNickname)
        return "[NICKNAME_TAKEN]"

    const oldNickname = await getNickname(account.publicKey);
    if (oldNickname !== "")
        await removeOldNickname(oldNickname.toLowerCase());

    await setNewNickname(account.publicKey, inputNickname);
    return inputNickname;
});
