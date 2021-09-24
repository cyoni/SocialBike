
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
        // if (user_nickname === null)
        //      user_nickname = "No Nickname";
        return user_nickname
    })
}


function removeOldNickname(oldNickname) {
    return admin.database().ref('nicknames').child(oldNickname).remove();
}


exports.AddNewPost = functions.https.onCall(async (snapshot, context) => {

    const myPrivateKey = context.auth.uid;
    const user_message = snapshot.message.trim();
    const groupId = snapshot.groupId
    const eventId = snapshot.eventId || null

    const now = Date.now();

    if (user_message.length > 5000)
        return "TOO_LONG";

    const account = await verifyUser(myPrivateKey);
    if (account === null)
        return "AUTH_FAILED"

    const myPublicKey = account.publicKey;

    var data = {
        message: user_message,
        timestamp: now,
        user_public_key: myPublicKey
    };

    var route

    if (groupId !== null && eventId === null)
        route = admin.database().ref('groups').child(groupId).child('posts')
    else if (groupId !== null && eventId !== null)
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId).child('posts')
    else if (groupId === null && eventId !== null)
        route = admin.database().ref('events').child(eventId).child('posts')
    else
        return "failure"

    const post_id = await route.push().key
    const set_data = route.child(post_id).set(data);

    const ref = admin.database().ref('public').child(myPublicKey).child('profile').child('posts_count');
    const incrementMyPostsCounter = ref.transaction((current) => {
        return (current || 0) + 1;
    });

    await set_data
    await incrementMyPostsCounter
    return post_id;
});

exports.updateProfile = functions.https.onCall(async (request, context) => {
    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "[AUTH_FAILED]";

    const publicKey = account.publicKey

    var lat = request.lat || null
    var lng = request.lng || null
    var country = request.country || null
    var city = request.city || null

    if (lat === null || lng === null) {
        lat = null
        lng = null
    }

    const gender = request.gender || null
    const age = request.age || null

    const a = admin.database().ref('public').child(publicKey).child('profile').set({
        age: age,
        gender: gender
    });

    const b = admin.database().ref('public').child(publicKey).child('profile').child('preferred_location').set({
        lat: lat,
        lng: lng,
        country: country,
        city: city
    })

    await a, b
    return "OK"
})

exports.getPosts = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid)
    const userPublicId = account.publicKey

    var data = {}
    data['posts'] = []

    const groupId = request.groupId || null
    const eventId = request.eventId || null

    var route

    if (groupId === null && eventId === null)
        return "bad_request"
    else if (groupId !== null && eventId === null) {
        route = admin.database().ref('groups').child(groupId)
    } else if (groupId !== null && eventId !== null)
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId)
    else if (groupId === null && eventId !== null)
        route = admin.database().ref('events').child(eventId)


    return route.child('posts').once('value').then(snapshot => {
        snapshot.forEach(raw_post => {

            var post = []
            post = ({
                postId: raw_post.key,
                publicKey: raw_post.child('user_public_key').val(),
                name: "...",
                message: raw_post.child('message').val(),
                timestamp: raw_post.child('timestamp').val(),
            })

            if (raw_post.child('comments').exists())
                post['comments_count'] = raw_post.child('comments').numChildren()

            if (raw_post.child('likes_count').exists()) {
                post['likes_count'] = raw_post.child('likes_count').val()
                post['doesUserLikeThePost'] = raw_post.child('likes').child(userPublicId).exists()
            }

            data['posts'].push(post)
        })
        return JSON.stringify(data)
    })
})

exports.getComments = functions.https.onCall(async (request, context) => {

    const postId = request.postId
    const groupId = request.groupId
    const eventId = request.eventId

    var route;
    if (groupId === null && eventId === null)
        return "bad_request"
    else if (groupId !== null && eventId === null) {
        route = admin.database().ref('groups').child(groupId)
    } else if (groupId !== null && eventId !== null)
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId)
    else if (groupId === null && eventId !== null)
        route = admin.database().ref('events').child(eventId)

    var data = {}
    data['posts'] = []
    var counter = 0;

    return route.child('posts').child(postId).child('comments').once('value').then(snapshot => {

        data.posts[counter] = []

        snapshot.forEach(raw_post => {
            var commentData = {
                commentId: raw_post.key,
                postId: postId,
                publicKey: raw_post.child('publicKey').val(),
                message: raw_post.child('comment').val(),
                timestamp: raw_post.child('timestamp').val(),
                comments_count: raw_post.child('comments').val(),
                likes_count: raw_post.child('likes').numChildren()
            }

            commentData['subComments'] = []

            if (raw_post.child('comments').exists()) {
                raw_post.child('comments').forEach(subComment => {
                    var subCommentToInsert = {
                        commentId: subComment.key,
                        postId: postId,
                        publicKey: subComment.child('publicKey').val(),
                        message: subComment.child('comment').val(),
                        timestamp: subComment.child('timestamp').val(),
                        likes_count: raw_post.child('likes').numChildren()
                    }
                    commentData.subComments.push(subCommentToInsert)
                })
                commentData.subComments = commentData.subComments.reverse()
            }
            data.posts[counter] = commentData
            counter++
        })

        data.posts = data.posts.reverse()
        return JSON.stringify(data)
    })
})


function compare(a, b) {
    if (a.elementScore < b.elementScore) {
        return -1;
    }
    if (a.elementScore > b.elementScore) {
        return 1;
    }
    return 0;
}

// Converts numeric degrees to radians
function toRad(Value) {
    return Value * Math.PI / 180;
}

function distanceFromMe(lat1, lon1, lat2, lon2) {
    var R = 6371; // km
    var dLat = toRad(lat2 - lat1);
    var dLon = toRad(lon2 - lon1);
    lat1 = toRad(lat1);
    lat2 = toRad(lat2);

    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c
}

exports.getEvents = functions.https.onCall(async (request, context) => {

    const dataType = request.dataType
    var data = {}
    data['events'] = []

    const lat = request.lat
    const lng = request.lng
    const range = request.range
    const country = request.country || null
    const groupId = request.groupId || null

    var ref;
    if (groupId !== null) {
        ref = admin.database().ref('groups').child(groupId).child('events')
    }
    else
        ref = admin.database().ref('events')

    return ref.once('value').then(snapshot => {
        snapshot.forEach(raw_data => {

            if (
                groupId !== null ||
                distanceFromMe(raw_data.child('lat').val(), raw_data.child('lng').val(), lat, lng) <= range ||
                range === 100 && raw_data.child('country').val() === country
            ) {

                var dataOfEvent = {
                    event_id: raw_data.key,
                    name: "...",
                    user_public_key: raw_data.child('user_public_key').val(),
                    details: raw_data.child('details').val(),
                    created_event_time: raw_data.child('created_event_time').val(),
                    date: raw_data.child('date').val(),
                    time: raw_data.child('time').val(),
                    num_interested_members: raw_data.child('num_interested_members').val(),
                    num_participants: raw_data.child('num_participants').val(),
                    lat: raw_data.child('lat').val(),
                    lng: raw_data.child('lng').val(),
                    title: raw_data.child('title').val(),
                    address: raw_data.child('address').val(),
                    comments_num: raw_data.child('comments').numChildren(),
                }

                if (groupId !== null)
                    dataOfEvent["group_id"] = groupId

                const score = 2 * dataOfEvent.num_participants + dataOfEvent.num_interested_members
                dataOfEvent['elementScore'] = score

                data['events'].push(dataOfEvent)
            }
        })

        if (dataType === "TRADING") {
            data.events = data.events.sort(compare)
        }

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

    const result = await admin.database().ref('nicknames').once('value')
    const takenNickname = (result.child(inputNickname).exists());

    if (takenNickname === true)
        return "[NICKNAME_TAKEN]"
    const oldNickname = await getNickname(account.publicKey);
    if (oldNickname !== "")
        removeOldNickname(oldNickname);
    await setNewNickname(account.publicKey, inputNickname);
    return inputNickname;
});


exports.AddNewEvent = functions.https.onCall(async (request, context) => {
    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const groupId = request.groupId || null

    if (account === null)
        return "[AUTH_FAILED]";

    var ref;

    if (groupId !== null) {
        ref = admin.database().ref('groups').child(groupId).child('events')
    }
    else
        ref = admin.database().ref('events')

    const time = request.time
    const date = request.date
    const details = request.details
    const lat = request.lat
    const lng = request.lng
    const title = request.title
    const address = request.address
    const country = request.country
    const city = request.city
    const timestamp = Date.now()

    var data = {
        user_public_key: account.publicKey,
        created_event_time: timestamp,
        time: time,
        date: date,
        title: title,
        address: address,
        details: details,
        lat: lat,
        lng: lng,
        country: country,
        city: city,
    }

    const newKey = ref.push().key
    await ref.child(newKey).set(data)

    return "OK"
})

exports.sendPrivateMsg = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);

    if (account === null)
        return "[AUTH_FAILED]";

    const receiverPublicKey = request.receiver;
    const publicKey = account.publicKey
    const message = request.message
    const timestamp = Date.now()


    const sendersName = await getNickname(account.publicKey)

    const verifyReciever = await getNickname(receiverPublicKey)

    if (verifyReciever === null) {
        return "ERR:NO_USER"
    }

    const data = {
        receiverPublicKey: receiverPublicKey,
        publicKey: publicKey,
        message: message,
        sendersName: sendersName,
        timestamp: timestamp,
    }

    const messageId = admin.database().ref('private_msgs')
        .child(receiverPublicKey)
        .push().key

    admin.database().ref('private_msgs')
        .child(receiverPublicKey)
        .child(messageId)
        .child(senderPublicKey)
        .set(data)
    return "OK"
})


exports.updateParticipantsNumber = functions.database.ref('events/{eventId}/{going_or_interested}/{userId}').onWrite(
    async (change, context) => {

        var childName
        if (context.params.going_or_interested === "going")
            childName = "num_participants"
        else
            childName = "num_interested_members"
        const collectionRef = change.after.ref.parent;
        const countRef = collectionRef.parent.child(childName);

        if (!change.after.exists() && change.before.exists() && change.before.val <= 0) {
            console.log('Exiting function . . . ')
            return null;
        }

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
    const replyTo = request.replyTo || null
    const groupId = request.groupId || null
    const eventId = request.eventId || null

    var route

    if (groupId === null && eventId !== null) { // send comment to 'events' bucket
        route = admin.database().ref('events').child(eventId)
    } else if (groupId !== null && eventId === null) { // send comment to group -> posts
        route = admin.database().ref('groups').child(groupId)
    } else if (groupId !== null && eventId !== null) { // send comment to group -> events -> posts
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId)
    }
    else
        return "VALIDATION_FAILURE"

    route = route.child('posts').child(postId).child('comments')

    const data = {
        publicKey: account.publicKey,
        comment: comment,
        timestamp: Date.now(),
    }

    var messageId

    if (replyTo === null) {
        messageId = await route.push().key
        route.child(messageId).set(data)
    }
    else {
        route = route.child(replyTo).child('comments')
        messageId = await route.push().key
        route.child(messageId).set(data)
    }

    return messageId
})


exports.interested = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const eventId = request.eventId

    if (account === null)
        return "[AUTH_FAILED]"

    const a = await admin.database().ref('events').child(eventId).child('interested').child(account.publicKey).once('value')
    if (a.exists()) {
        admin.database().ref('events').child(eventId).child('interested').child(account.publicKey).remove()
    }
    else
        await admin.database().ref('events').child(eventId).child('interested').child(account.publicKey).set(Date.now())
    return "OK"

})

exports.going = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const eventId = request.eventId

    if (account === null)
        return "[AUTH_FAILED]";

    const a = await admin.database().ref('events').child(eventId).child('going').child(account.publicKey).once('value')
    if (a.exists()) {
        admin.database().ref('events').child(eventId).child('going').child(account.publicKey).remove()
    }
    else
        await admin.database().ref('events').child(eventId).child('going').child(account.publicKey).set(Date.now())
    return "OK"

})


exports.getMemberList = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const eventId = request.eventId
    const keyName = request.keyName

    if (account === null)
        return "[AUTH_FAILED]"

    if (keyName !== "going" && keyName !== "interested")
        return "[FAIL]"

    var data = {}
    data['members'] = []
    return admin.database().ref('events').child(eventId).child(keyName).once('value').then(snapshot => {

        snapshot.forEach(raw_data => {
            data.members.push(raw_data.key)
        })
        return admin.database().ref('public').once('value')
    }).then(snapshot => {
        for (var i = 0; i < data.members.length; i++)
            data.members[i] = snapshot.child(data.members[i]).child('profile').child('nickname').val()

        data.members = data.members.reverse()
        return JSON.stringify(data)
    })

})


exports.getPlaces = functions.https.onCall(async (request, context) => {

    const lat = request.lat
    const lng = request.lng
    const range = request.range
    const country = request.country

    //   if (!isCountry(country))
    //     return "[false]"

    var events = []

    return admin.database().ref('events').once('value').then(snapshot => {
        snapshot.forEach(raw_data => {
            if (raw_data.child('country').val() === country
                && distanceFromMe(raw_data.child('lat').val(), raw_data.child('lng').val(), lat, lng) <= range) {
                events.push(raw_data)
            }
        })
        return null
    }).then(x => {
        return JSON.stringify(events)
    })
})

exports.findUsers = functions.https.onCall(async (request, context) => {

    const name = request.name.toLowerCase().trim()

    if (name.length <= 2) {
        return "TOO_SHORT"
    }

    var array = {}
    array['users'] = []

    return admin.database().ref('nicknames').once('value').then(snapshot => {
        snapshot.forEach(raw_data => {
            if (raw_data.key.toLowerCase().includes(name)) {
                array['users'].push(
                    {
                        name: raw_data.key,
                        userId: raw_data.val()
                    })
            }
        })
        return null
    }).then(x => {
        return JSON.stringify(array)
    })
})


exports.LikePostInEvent = functions.database.ref('events/{eventId}/posts/{postId}/likes/{userId}'). onWrite(
    async (change) => {
        return LikeHandler(change)
    })

exports.LikePost = functions.database.ref('groups/{groupId}/posts/{postId}/likes/{userId}').onWrite(
    async (change) => {
        return LikeHandler(change)
    })

exports.LikeEventInGroup = functions.database.ref('groups/{groupId}/{events}/{id}/posts/{postId}/likes/{userId}').onWrite(
    async (change) => {
        return LikeHandler(change)
    });


async function LikeHandler(change) {
    const collectionRef = change.after.ref.parent.parent.child('likes_count')
    //const countRef = collectionRef.parent.child('likes_count');

    let increment;

    if (change.before.exists() && !change.after.exists()) {
        return;
    }

    if (change.after.exists() && !change.before.exists()) {
        increment = 1;
    } else if (!change.after.exists() && change.before.exists()) {
        increment = -1;
    } else {
        return null;
    }

    await collectionRef.transaction((current) => {
        if ((current || 0) + increment < 0)
            return (increment > 0) ? 1 : 0
        else
            return (current || 0) + increment;
    });
    functions.logger.log('Counter updated.');
    return null;
}


exports.commentCountTrigger = functions.database.ref('global_posts/{postId}/comments/{commentId}').onWrite(
    async (change) => {
        const collectionRef = change.after.ref.parent;
        const countRef = collectionRef.parent.child('comments_count');

        let increment;

        if (change.before.exists() && !change.after.exists() && !collectionRef.parent('timestamp').exists()) {
            return;
        }

        if (change.after.exists() && !change.before.exists()) {
            increment = 1;
        } else if (!change.after.exists() && change.before.exists()) {
            increment = -1;
        } else {
            return null;
        }

        await countRef.transaction((current) => {
            if ((current || 0) + increment < 0)
                return (increment > 0) ? 1 : 0
            else
                return (current || 0) + increment;
        });
        functions.logger.log('Counter updated.');
        return null;
    });


exports.CreateGroup = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const title = request.title
    const description = request.description

    const data = {
        publicKey: account.publicKey,
        title: title,
        description: description
    }

    if (account === null)
        return "[AUTH_FAILED]";

    await admin.database().ref('groups').push(data)
    return "OK"

})


exports.GetMyGroups = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid)

    var data = {}
    data['groups'] = []
    var groups = []

    return admin.database().ref('public').child(account.publicKey).child('connected_groups').once('value').then(snapshot => {
        snapshot.forEach(raw_post => {
            groups.push(raw_post.key)
        })
        return admin.database().ref('groups').once('value')
    }).then(snapshot => {
        snapshot.forEach(raw_post => {
            if (groups.includes(raw_post.key)) {
                data['groups'].push(getGroupData(raw_post))
            }
        })
        return null;
    }).then(x => {
        return JSON.stringify(data)
    })
})

function getGroupData(snapshot) {
    return ({
        groupId: snapshot.key,
        title: snapshot.child('title'),
        description: snapshot.child('description'),
        memberCount: snapshot.child('members').numChildren()
    })
}


exports.GetAllGroups = functions.https.onCall(async (request, context) => {
    var data = {}
    data['groups'] = []

    return admin.database().ref('groups').once('value').then(snapshot => {
        snapshot.forEach(raw_post => {
            var group = []
            group = getGroupData(raw_post)
            data['groups'].push(group)
        })
        return JSON.stringify(data)
    })
})

exports.JoinGroup = functions.https.onCall(async (request, context) => {
    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "AUTH_FAILED"

    var groupId = request.groupId

    const a = admin.database().ref('public').child(account.publicKey).child('connected_groups').child(groupId).set(true)
    const b = admin.database().ref('groups').child(groupId).child('members').child(account.publicKey).set(Date.now())

    await a
    await b

    return "OK"
})

exports.LeaveGroup = functions.https.onCall(async (request, context) => {
    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "AUTH_FAILED"

    var groupId = request.groupId

    const a = admin.database().ref('public').child(account.publicKey).child('connected_groups').child(groupId).remove()
    const b = admin.database().ref('groups').child(groupId).child('members').child(account.publicKey).remove()

    await a
    await b

    return "OK"
})



exports.RegisterLike = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "AUTH_FAILED"

    const groupId = request.groupId || null
    const eventId = request.eventId || null
    const commentId = request.commentId || null
    const postId = request.postId || null

    

})


// deprecated
exports.GetGroupPosts = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid);
    if (account === null)
        return "AUTH_FAILED"

    const groupId = request.groupId
    const eventId = request.eventId || null

    var data = {}
    data['posts'] = []

    var route = admin.database().ref('groups').child(groupId)

    if (eventId !== null) {
        route = route.child('events').child(eventId)
    }

    return route.child('posts').once('value').then(snapshot => {
        snapshot.forEach(raw_post => {

            var dataOfUser = ({
                postId: raw_post.key,
                publicKey: raw_post.child('user_public_key').val(),
                name: "...",
                message: raw_post.child('message').val(),
                timestamp: raw_post.child('timestamp').val(),
            })

            if (raw_post.child('comments_count').exists()) {
                dataOfUser['comments_count'] =
                    raw_post.child('comments_count').val()
            }

            if (raw_post.child('likes_count').exists()) {
                dataOfUser['likes_count'] = raw_post.child('likes_count').val()
                dataOfUser['doesUserLikeThePost'] = raw_post.child('likes').child(userPublicId).exists()
            }

            data['posts'].push(dataOfUser)
        })
        return JSON.stringify(data)
    })
})
 /*
 exports.recountlikes = functions.database.ref('global_posts/{postid}/likes_count').onDelete(async (snap) => {
   const counterRef = snap.ref;
   const collectionRef = counterRef.parent.child('likes');

   const messagesData = await collectionRef.once('value');
   return await counterRef.set(messagesData.numChildren());
 });
 */
