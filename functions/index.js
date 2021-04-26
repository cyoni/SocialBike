
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const util = require('util');

admin.initializeApp();

exports.addUserToDB = functions.auth.user().onCreate((event) => {
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
    ``
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
        var user_nickname = res.val();
        if (user_nickname === null)
            user_nickname = "No Nickname";
        return user_nickname
    })
}


function removeOldNickname(oldNickname) {
    return admin.database().ref('nicknames').child(oldNickname).remove();
}


exports.AddNewPost = functions.https.onCall(async (snapshot, context) => {

    const myPrivateKey = context.auth.uid;
    const user_message = snapshot.message.trim();
    const now = Date.now();

    if (user_message.length > 5000)
        return "FAIL";

    const account = await verifyUser(myPrivateKey);
    if (account === null)
        return "AUTH_FAILED"

    const myPublicKey = account.publicKey;

    var data = {
        message: user_message,
        timestamp: now,
        user_public_key: myPublicKey,
    };

    const container = 'global_posts'
    const post_id = await (admin.database().ref(container).push().key)
    const set_data = admin.database().ref(container).child(post_id).set(data);
    //  const set_category = admin.database().ref('user_public').child(myPublicKey).child(where).child(post_id).set(category);
    const ref = admin.database().ref('public').child(myPublicKey).child('profile').child('posts_count');
    const incrementMyPostsCounter = ref.transaction((current) => {
        return (current || 0) + 1;
    });

    await set_data, incrementMyPostsCounter;
    return post_id;

});

exports.updateProfile = functions.https.onCall(async (request, context) => {
    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "[AUTH_FAILED]";

    const publicKey = account.publicKey

    const country = request.country;
    const city = request.city;
    const gender = request.gender;
    const age = request.age;

    const a = admin.database().ref('public').child(publicKey).child('profile').child('country').set(country);
    const b = admin.database().ref('public').child(publicKey).child('profile').child('city').set(city);
    const c = admin.database().ref('public').child(publicKey).child('profile').child('gender').set(gender);
    const d = admin.database().ref('public').child(publicKey).child('profile').child('age').set(age);

    await a, b, c, d
    return "OK"
})

exports.getPosts = functions.https.onCall(async (request, context) => {

    var data = {}
    data['posts'] = []
    let counter = 0;

    return admin.database().ref('global_posts').once('value').then(snapshot => {
        snapshot.forEach(raw_post => {
           
            
            var dataOfUser = {
                publicKey: raw_post.child('user_public_key').val(),
                name: "@@",
                message: raw_post.child('message').val(),
                timestamp: raw_post.child('timestamp').val()
            }
            data.posts[counter++] = dataOfUser

        })
        return admin.database().ref('public').once('value')

    }).then(da => {

        for (var i = 0; i < data.posts.length; i++) {
            data.posts[i].name = da.child(data.posts[i].publicKey).child('profile').child('nickname').val()
        }

        return util.inspect(data, { showHidden: false, depth: null }) 
    })
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
