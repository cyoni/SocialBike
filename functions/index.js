
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
                postId: raw_post.key,
                publicKey: raw_post.child('user_public_key').val(),
                name: "",
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

        return JSON.stringify(data)
    })
})

exports.getComments = functions.https.onCall(async (request, context) => {

    const postId = request.postId
    var data = {}
    data['posts'] = []
    var counter = 0;

    return admin.database().ref('global_posts').child(postId).child('comments').once('value').then(snapshot => {

        snapshot.forEach(raw_post => {
            var dataOfUser = {
                postId: raw_post.key,
                publicKey: raw_post.child('senderPublicKey').val(),
                name: "",
                message: raw_post.child('comment').val(),
                timestamp: raw_post.child('timestamp').val()
            }
            data.posts[counter++] = dataOfUser

        })
        return admin.database().ref('public').once('value')

    }).then(da => {

        for (var i = 0; i < data.posts.length; i++) {
            data.posts[i].name = da.child(data.posts[i].publicKey).child('profile').child('nickname').val()
        }
        
        data.posts = data.posts.reverse()

        return JSON.stringify(data)
    })
})


exports.getEvents = functions.https.onCall(async (request, context) => {

    var data = {}
    data['events'] = []
    var counter = 0;
    // const location = 

    return admin.database().ref('events').once('value').then(snapshot => {
        snapshot.forEach(raw_data => {

            const dataOfEvent = {
                eventId: raw_data.key,
                name: "",
                userPublicKey: raw_data.child('userpublicKey').val(),
                eventContent: raw_data.child('eventContent').val(),
                createdEventTime: raw_data.child('createdEventTime').val(),
                eventDate: raw_data.child('eventDate').val(),
                eventTime: raw_data.child('eventTime').val(),
                amountOfInterestedPeople: raw_data.child('amountOfInterestedPeople').val(),
                eventCity: raw_data.child('eventCity').val(),
                eventCountry: raw_data.child('eventCountry').val(),
                // coordinates
            }

            data.events[counter++] = dataOfEvent
        })
        return admin.database().ref('public').once('value')
    }).then(snapshot => {
        for (var i = 0; i < data.events.length; i++) 
            data.events[i].name = snapshot.child(data.events[i].userPublicKey).child('profile').child('nickname').val()

        data.events = data.events.reverse()
        return JSON.stringify(data) 
    })
})

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
        removeOldNickname(oldNickname.toLowerCase());
    await setNewNickname(account.publicKey, inputNickname);
    return inputNickname;
});


exports.AddNewEvent = functions.https.onCall(async (request, context) => {
    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);

    if (account === null)
        return "[AUTH_FAILED]";

    const eventTime = request.time
    const eventDate = request.date
    const eventContent = request.content
    const eventCity = request.city
    const eventCountry = request.country
    const timestamp = Date.now()

    var data = {
        userPublicKey: account.publicKey,
        createdEventTime: timestamp,
        eventTime: eventTime,
        eventDate: eventDate,
        eventContent: eventContent,
        eventCity: eventCity,
        eventCountry: eventCountry
    }

    const newKey = admin.database().ref('events').push().key
    await admin.database().ref('events').child(newKey).set(data)

    return "OK"
})

exports.sendPrivateMsg = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);

    if (account === null)
        return "[AUTH_FAILED]";

    const receiverPublicKey = request.receiver;
    const senderPublicKey = account.publicKey
    const message = request.message
    const timestamp = Date.now()

    const sendersName = await getNickname(account.publicKey)

    const data = {
        receiverPublicKey: receiverPublicKey,
        senderPublicKey: senderPublicKey,
        message: message,
        sendersName: sendersName,
        timestamp: timestamp,
    }

    const messageId = await admin.database().ref('private_msgs').child(receiverPublicKey).push().key
    admin.database().ref('private_msgs').child(receiverPublicKey).child(messageId).child(senderPublicKey).set(data)
    return "OK"
})


exports.interestedPerson = functions.database.ref('/posts/{postid}/likes/{likeid}').onWrite(
    async (change) => {
      const collectionRef = change.after.ref.parent;
      const countRef = collectionRef.parent.child('likes_count');

      let increment;
      if (change.after.exists() && !change.before.exists()) {
        increment = 1;
      } else if (!change.after.exists() && change.before.exists()) {
        increment = -1;
      } else {
        return null;
      }

      // Return the promise from countRef.transaction() so our function
      // waits for this async event to complete before it exits.
      await countRef.transaction((current) => {
        return (current || 0) + increment;
      });
      return null;
    });


exports.sendComment = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);

    if (account === null)
        return "[AUTH_FAILED]";

    const comment = request.comment
    const postId = request.postId
    const replyTo = request.replyTo.trim()

    const data = {
        senderPublicKey: account.publicKey,
        comment: comment,
     //   postId: postId,
        timestamp: Date.now(),
    }

    var messageId;

    if (replyTo !== ""){
        messageId = await admin.database().ref('global_posts').child(postId).child('comments').child(replyTo).push().key
        admin.database().ref('global_posts').child(postId).child('comments').child(replyTo).child(messageId).set(data)
    }
    else{
        messageId = await (await admin.database().ref('global_posts').child(postId).child('comments').push()).key
        admin.database().ref('global_posts').child(postId).child('comments').child(messageId).set(data)
    }

    return messageId
})




