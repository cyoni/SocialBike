
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const util = require('util');
const { getgroups } = require("process");
const { group } = require("console");

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
                publicKey: snapshot.child('userPublicKey').val(),
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
    if (oldNickname === null || oldNickname === "")
        return;
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


    const await_age = admin.database().ref('public').child(publicKey).child('profile').child('age').set(age);
    const await_gender = admin.database().ref('public').child(publicKey).child('profile').child('gender').set(gender);

    const await_preferred_location = admin.database().ref('public').child(publicKey).child('profile').child('preferred_location').set({
        lat: lat,
        lng: lng,
        country: country,
        city: city
    })

    await await_age
    await await_gender
    await await_preferred_location

    return "OK"
})

exports.getPosts = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid)
    var userPublicId
    if (account !== null)
        userPublicId = account.publicKey

    var data = {}
    data['posts'] = []

    const groupId = request.groupId || null
    const eventId = request.eventId || null
    const getFirstPost = request.getFirstPost || null

    var route

    if (groupId === null && eventId === null)
        return "bad_request"
    else if (groupId !== null && eventId === null) {
        route = admin.database().ref('groups').child(groupId)
    } else if (groupId !== null && eventId !== null)
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId)
    else if (groupId === null && eventId !== null)
        route = admin.database().ref('events').child(eventId)

    route = route.child('posts')

    if (getFirstPost){
        route = route.limitToLast(1)
    }

    return route.once('value').then(snapshot => {
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
                    if (userPublicId !== null)
                        post['isLiked'] = raw_post.child('likes').child(userPublicId).exists()
                }

                data['posts'].push(post)
        })
   
        data.posts = data.posts.reverse()
        return JSON.stringify(data)
    })
})

exports.getComments = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid)
    const userPublicId = account.publicKey

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
                comments_count: raw_post.child('comments_count').val(),
            }

            if (raw_post.child('likes').exists()) {
                commentData['likes_count'] = raw_post.child('likes').numChildren()
                if (userPublicId !== null)
                    commentData['isLiked'] = raw_post.child('likes').child(userPublicId).exists()
            }

            commentData['subComments'] = []

            if (raw_post.child('comments').exists()) {
                raw_post.child('comments').forEach(subComment => {
                    var subCommentToInsert = {
                        subCommentId: subComment.key,
                        publicKey: subComment.child('publicKey').val(),
                        message: subComment.child('comment').val(),
                        timestamp: subComment.child('timestamp').val(),
                    }

                    if (raw_post.child('likes').exists()) {
                        subCommentToInsert['likes_count'] = subComment.child('likes').numChildren()
                        if (userPublicId !== null)
                            subCommentToInsert['isLiked'] = subComment.child('likes').child(userPublicId).exists()
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

function makeEventObject(raw_data, groupId, publicKey="_"){
    console.log("in make event object. " + groupId + "," + publicKey)
    var dataOfEvent = {
        event_id: raw_data.key,
        publicKey: raw_data.child('user_public_key').val(),
        details: raw_data.child('details').val(),
        created_event_time: raw_data.child('created_event_time').val(),
        start: raw_data.child('start').val(),
        end: raw_data.child('end').val(),
        num_interested_members: raw_data.child('interested').numChildren(),
        num_participants: raw_data.child('going').numChildren(),
        lat: raw_data.child('lat').val(),
        lng: raw_data.child('lng').val(),
        title: raw_data.child('title').val(),
        address: raw_data.child('address').val(),
        comments_num: raw_data.child('comments').numChildren(),
        isGoing: raw_data.child('going').child(publicKey).exists(),
        isInterested: raw_data.child('interested').child(publicKey).exists(),
    }

   if (raw_data.child('header_picture').child('has_header_picture').exists()){
        dataOfEvent['has_header_picture'] = raw_data.child('header_picture').child('has_header_picture').val() === true ? true : false
        dataOfEvent['picture_header_created'] = raw_data.child('header_picture').child('created').val() !== null ? raw_data.child('header_picture').child('created').val() : 0
   }

    if (groupId !== null)
        dataOfEvent["group_id"] = groupId

    const score = 2 * dataOfEvent.num_participants + dataOfEvent.num_interested_members
    dataOfEvent['elementScore'] = score

    console.log("makeEventObject - finished.")

    return dataOfEvent
}


exports.getEvents = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid)
    var publicKey = "_"
    if (account !== null)
        publicKey = account.publicKey

    const dataType = request.dataType
    var data = {}
    data['events'] = []
    data['extra_events'] = []

    const lat = request.lat
    const lng = request.lng
    const range = 100
    const country = request.country || null
    const groupId = request.groupId || null
    const getFirstEvent = request.getFirstEvent || null

    const specificEvents = request.specificEvents || null

    var ref;

    if (groupId !== null) {
        ref = admin.database().ref('groups').child(groupId).child('events')
    }
    else
        ref = admin.database().ref('events')

        
    return admin.database().ref('groups').once('value').then(snapshot => {
        // get events from groups 
        // take my groups
        // take their events
        // sort everything

        console.log("getting extra events. groupId: " + groupId)
        if (groupId === null)
        {
            console.log("enters extra zone scope. snapshot: " + snapshot.numChildren())

            var group_events = []
            
            snapshot.forEach(raw_data => {
                if (raw_data.child('members').child(publicKey).exists()){
                    var groupEvents = raw_data.child('events')
                    if (groupEvents.exists()){
                        groupEvents.forEach(current => {
                            // if current is active
                            if (
                                (specificEvents !== null && specificEvents.includes(current.key) || 
                                specificEvents === null && raw_data.child('publicKey').exists()) &&
                                (distanceFromMe(current.child('lat').val(), current.child('lng').val(), lat, lng) <= range)
                                ){
                                group_events.push( makeEventObject(current, raw_data.key, publicKey)  )
                            }
                        })
                    }
                }
            })

            group_events.forEach(current => {
                data['events'].push(current)
            })
      
        }


        if (getFirstEvent)
            ref = ref.limitToLast(1)
        
        
        return ref.orderByChild('created_event_time').once('value')

    }).then(snapshot => { 

        snapshot.forEach(raw_data => {

            if (
                specificEvents !== null && specificEvents.includes(raw_data.key) ||
                groupId !== null ||
                distanceFromMe(raw_data.child('lat').val(), raw_data.child('lng').val(), lat, lng) <= range 
                     && raw_data.child('country').val() === country
                     && raw_data.child("user_public_key").exists() 
            ) {

                if (raw_data.child('user_public_key').exists())
                {
                    var eventObject = makeEventObject(raw_data, groupId, publicKey)
                    data['extra_events'].push(eventObject)
                }
                

            }
        })



        if (dataType === "TRENDING") {
            data.events = data.events.sort(compare)
        }

        data.extra_events = data.extra_events.reverse()
        data.events = data.events.reverse()
        return JSON.stringify(data)

    })
})


exports.updateNickname = functions.https.onCall(async (data, context) => {
    console.log("updatenickname ")

    const privateKey = context.auth.uid || null;
    var inputNickname = data.nickname.trim();
    console.log("updatenickname - verifying  user: " + privateKey)

    const account = await verifyUser(privateKey);

    console.log("updatenickname - verified ")

    if (account === null)
        return "[AUTH_FAILED]";
    console.log("aa ")

    const validNickname = isNicknameValid(inputNickname);
    if (!validNickname)
        return "[INVALID_NICKNAME]"
    console.log("bb ")

    const result = await admin.database().ref('nicknames').once('value')
    console.log("cc ")

    const takenNickname = (result.child(inputNickname).exists());
    console.log("dd ")

    if (takenNickname === true)
        return "[NICKNAME_TAKEN]"
    console.log("e ")

    const oldNickname = await getNickname(account.publicKey);
    console.log("f")

    if (oldNickname !== "")
        removeOldNickname(oldNickname);
    console.log("1 ")
    await setNewNickname(account.publicKey, inputNickname);
    console.log("2")
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

    const start = request.start
    const end = request.end
    const details = request.details
    const lat = request.lat
    const lng = request.lng
    const title = request.title
    const address = request.address
    const country = request.country
    const city = request.city
    const timestamp = Date.now()

    if (start > end)
        return "BAD_DATES"
    

    var data = {
        user_public_key: account.publicKey,
        created_event_time: timestamp,
        start: start,
        end: end,
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

    return newKey
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
        .child(publicKey)
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

function GetReference(eventId, groupId){

    var route;

    if (groupId === null && eventId !== null) { // send comment to 'events' bucket
        route = admin.database().ref('events').child(eventId)
    } else if (groupId !== null && eventId === null) { // send comment to group -> posts
        route = admin.database().ref('groups').child(groupId)
    } else if (groupId !== null && eventId !== null) { // send comment to group -> events -> posts
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId)
    }

    return route;
}
exports.interested = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const eventId = request.eventId
    const groupId = request.groupId

    if (account === null)
        return "[AUTH_FAILED]"

    var ref = GetReference(eventId, groupId).child('interested').child(account.publicKey)

    const a = await ref.once('value')
    if (a.exists()) {
        ref.remove()
    }
    else
        await ref.set(Date.now())
    return "OK"

})

exports.going = functions.https.onCall(async (request, context) => {

    const privateKey = context.auth.uid;
    const account = await verifyUser(privateKey);
    const eventId = request.eventId
    const groupId = request.groupId || null

    if (account === null)
        return "[AUTH_FAILED]";

    const ref = GetReference(eventId, groupId).child('going').child(account.publicKey)
    const a = await ref.once('value')
    if (a.exists()) {
        ref.remove()
    }
    else
        ref.set(Date.now())
    return "OK"

})


exports.getMemberList = functions.https.onCall(async (request, context) => {

    const eventId = request.eventId || null
    const groupId = request.groupId || null
    const keyName = request.keyName

    if (keyName !== "going" && keyName !== "interested")
        return "[FAIL]"

    var route
    if (groupId !== null)
        route = admin.database().ref('groups').child(groupId).child('events').child(eventId).child(keyName)
    else 
        route = admin.database().ref('events').child(eventId).child(keyName)

    var data = {}
    data['members'] = []
    return route.once('value').then(snapshot => {

        snapshot.forEach(raw_data => {
            data.members.push(raw_data.key)
        })
        
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
    
    const lat = request.lat
    const lng = request.lng

    const data = {
        publicKey: account.publicKey,
        title: title,
        description: description,
        lat: lat,
        lng: lng,
        timestamp: Date.now()
    }

    if (account === null)
        return "[AUTH_FAILED]";

    /*
    if (country === null && city === null){
        return "country and city are null"
    } else if (city === null || city.trim() === "" && country !== null && country.trim() !== "")
    {
        await admin.database().ref('groups').child(country).push(data)
    } else if (country === null || country.trim() === "" && city !== null && city.trim() !== "")
    {
        await admin.database().ref('groups').child('no_country').child(city).push(data)
    }
    else
    {
        await admin.database().ref('groups').child(country).child(city).push(data)
    }
    */

    await admin.database().ref('groups').push(data)
    return "OK"

})


exports.GetGroupsToPostNewEvent = functions.https.onCall(async (request, context) => {

    const account = await verifyUser(context.auth.uid)

    if (account === null){
        return "Access Denied.";
    }

    var data = {}
    data['groups'] = []

    return admin.database().ref('groups').once('value').then(snapshot => {
            snapshot.forEach(iter => {
                if (iter.child('publicKey').val() === account.publicKey)
                    data['groups'].push({
                        groupId: iter.key,
                        title: iter.child('title').val()
                    })
            })
            return JSON.stringify(data)
        })
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
        memberCount: snapshot.child('members').numChildren(),
        ownerOfGroup: snapshot.child('publicKey'),
        lat: snapshot.child('lat'),
        lng: snapshot.child('lng')
        })
}


exports.GetAllGroups = functions.https.onCall(async (request, context) => {
    const account = await verifyUser(context.auth.uid);
    const lat = request.lat
    const lng = request.lng
    const range = 100

    var data = {}
    data['groups'] = []

    return admin.database().ref('groups').once('value').then(snapshot => {
        snapshot.forEach(raw_group => {
            var group = getGroupData(raw_group)

            console.log(group.lat.val() + "," + group.lng.val() + "," + lat + "," + lng + "," + (distanceFromMe(group.lat, group.lng, lat, lng) <= range))
            if (distanceFromMe(group.lat.val(), group.lng.val(), lat, lng) <= range){
                if (account !== null){
                    console.log(account.publicKey + ", " + raw_group.key  + ", " + raw_group.child('members').child(account.publicKey).exists())
                    group['isMember'] = raw_group.child('members').child(account.publicKey).exists()
                }
                data['groups'].push(group)
            }
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
    const subCommentId = request.subCommentId || null

    var route

    if (groupId !== null){
        if (eventId === null)
            route = admin.database().ref('groups').child(groupId).child('posts').child(postId)
         else
            route = admin.database().ref('groups').child(groupId).child('events').child(eventId).child('posts').child(postId)
    }
    else if (eventId !== null){
        route = admin.database().ref('events').child(eventId).child('posts').child(postId)
    }

    if (commentId !== null){
        route = route.child('comments').child(commentId)
        if (subCommentId !== null)
            route = route.child('comments').child(subCommentId)
    }

    route = route.child('likes').child(account.publicKey)

    var increment
    const x = await route.once('value')
    if (x.exists()){
        // delete
        increment = -1
        route.remove()
    } else{
        // create
        increment = 1
        route.set(true)
    }
    
    const incrementMyPostsCounter = route.parent.parent.child('likes_count').transaction((current) => {
        return (current || 0) + increment;
    });

    await incrementMyPostsCounter

    return "OK"

})



/////////////////////////////////////////// firebase storage functions:



const mkdirp = require('mkdirp');
const path = require('path');
const os = require('os');
const fs = require('fs');
const { triggerAsyncId } = require("async_hooks");

exports.StorageInspector = functions.storage.object().onFinalize(async (object) => {
    const fileBucket = object.bucket; // The Storage bucket that contains the file.
    const filePath = object.name; // File path in the bucket.
    const contentType = object.contentType; // File content type.
    const metageneration = object.metageneration; // Number of times metadata has been generated. New objects have a value of 1.
    const fileName = path.basename(filePath);


   // if (fileName !== 'profile') { // fire only when you're uploading
  // console.log(filePath + "######" + fileName)
   
      // const account = await verifyUser(fileName);



        if (!contentType.startsWith('image/')) { // auth failed, remove uploaded picture
            const file_to_remove = bucket.file(filePath); // get a reference to the file
            await file_to_remove.delete();  // Delete the file
            return console.log("auth failed")
        }
      //  else {
          //  const publicKey = account.publicKey;
           
            const bucket = admin.storage().bucket(object.bucket);
            const file = bucket.file(filePath);
            const tempFilePath = path.join(os.tmpdir(), fileName);

            var groupId, eventId
            var splitRef = filePath.split("/")

            if (filePath.includes("groups") && filePath.includes("events")){
                groupId = splitRef[1]
                eventId = splitRef[3]
            } else if (filePath.includes("events")){
                eventId = splitRef[1]
            }

            var ref
            if (groupId && eventId)
                ref = admin.database().ref("groups").child(groupId).child("events").child(eventId)
            else 
                ref = admin.database().ref("events").child(eventId)
            
            return ref.child("header_picture").set({
                has_header_picture: true,
                created: Date.now()
            })

            /*
            const metadata = {
                contentType: contentType,
            };

            await bucket.file(filePath).download({ destination: tempFilePath });
            console.log('Image downloaded locally to', tempFilePath);
            const path_to_profile_dir = "users/" + publicKey;

            await bucket.upload(tempFilePath, {
                destination: path.join(path_to_profile_dir, "profile"),
                metadata: metadata,
            });
            fs.unlinkSync(tempFilePath); //delete tmp pic
            const file_to_remove = bucket.file(filePath); // Get a reference to the storage service, which is used to create references in your storage bucket
            await file_to_remove.delete(); // Delete uploaded file in 'tmp'

            await admin.database().ref('user_public').child(publicKey).child('profile').child('profileImage').set('t');
            */

           // return "OK";
        
   // }
   // else {
   //     console.log("already done")
  //  }
});


 /*
 exports.recountlikes = functions.database.ref('global_posts/{postid}/likes_count').onDelete(async (snap) => {
   const counterRef = snap.ref;
   const collectionRef = counterRef.parent.child('likes');

   const messagesData = await collectionRef.once('value');
   return await counterRef.set(messagesData.numChildren());
 });
 */
