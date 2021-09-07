
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
    const groupId = snapshot.groupId;

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

    const post_id = await (admin.database().ref('groups').child(groupId).child('posts').push().key)
    const set_data = admin.database().ref('groups').child(groupId).child('posts').child(post_id).set(data);

    const ref = admin.database().ref('public').child(myPublicKey).child('profile').child('posts_count');
    const incrementMyPostsCounter = ref.transaction((current) => {
        return (current || 0) + 1;
    });

    await set_data, incrementMyPostsCounter
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

    const account = await verifyUser(context.auth.uid)
    const userPublicId = account.publicKey

    var data = {}
    data['posts'] = []
    var count = 0

    return admin.database().ref('global_posts').once('value').then(snapshot => {
        snapshot.forEach(raw_post => {
           
            var dataOfUser = []
            dataOfUser = ({
                postId: raw_post.key,
                publicKey: raw_post.child('user_public_key').val(),
                name: "",
                message: raw_post.child('message').val(),
                timestamp: raw_post.child('timestamp').val(),
            })

            if (raw_post.child('comments_count').exists()){
                dataOfUser['comments_count'] = 
                     raw_post.child('comments_count').val()
            }

            if (raw_post.child('likes_count').exists()){
                dataOfUser['likes_count'] = raw_post.child('likes_count').val()
                dataOfUser['doesUserLikeThePost'] = raw_post.child('likes').child(userPublicId).exists()
            }
            

            data['posts'].push(dataOfUser)
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
    const container = request.container
     
    if (container !== "global_posts" && container !== "events")
        return "BAD REQUEST"
    
    var data = {}
    data['posts'] = []
    var counter = 0;

    return admin.database().ref(container).child(postId).child('comments').once('value').then(snapshot => {

        data.posts[counter] = []

        snapshot.forEach(raw_post => {
            var commentData = {
                postId: raw_post.key,
                publicKey: raw_post.child('senderPublicKey').val(),
                name: "",
                message: raw_post.child('comment').val(),
                timestamp: raw_post.child('timestamp').val()
            }


            commentData['subComments'] = []

            if (raw_post.child('subComments').exists()){

                raw_post.child('subComments').forEach(subComment => {

                    var subCommentToInsert = {
                        commentId: subComment.key,
                        senderPublicKey: subComment.child('senderPublicKey').val(),
                        name: "TODO",
                        comment: subComment.child('comment').val(),
                        timestamp: subComment.child('timestamp').val(),
                    }

                    commentData.subComments.push(subCommentToInsert)
                })
                commentData.subComments = commentData.subComments.reverse()
            }

            data.posts[counter] = commentData
            counter++
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


function compare(a, b) {
    if ( a.elementScore < b.elementScore ){
      return -1;
    }
    if ( a.elementScore > b.elementScore ){
      return 1;
    }
    return 0;
  }

      // Converts numeric degrees to radians
  function toRad(Value) 
  {
        return Value * Math.PI / 180;
  }

  function distanceFromMe(lat1, lon1, lat2, lon2){
    var R = 6371; // km
    var dLat = toRad(lat2-lat1);
    var dLon = toRad(lon2-lon1);
    lat1 = toRad(lat1);
    lat2 = toRad(lat2);

    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    return R * c
}

exports.getEvents = functions.https.onCall(async (request, context) => {

    const dataType = request.dataType
    var data = {}
    data['events'] = []
    var counter = 0;
    
    const lat = request.lat
    const lng = request.lng
    const range = request.range
    const country = request.country || null
    const state = request.state || null

 //   if (!isCountry(country))
   //     return "[false]"
       
    return admin.database().ref('events').once('value').then(snapshot => {
        snapshot.forEach(raw_data => {

            if ( ((country && raw_data.child('country').val() === country) 
                   //  || (state && raw_data.child('state').val() === state) 
                       ) 
                    && distanceFromMe(raw_data.child('lat').val(), raw_data.child('lng').val(), lat, lng) <= range){
                
            const dataOfEvent = {
                eventId: raw_data.key,
                name: "",
                userPublicKey: raw_data.child('userPublicKey').val(),
                eventDetails: raw_data.child('eventDetails').val(),
                createdEventTime: raw_data.child('createdEventTime').val(),
                eventDate: raw_data.child('eventDate').val(),
                eventTime: raw_data.child('eventTime').val(),
                numOfInterestedMembers: raw_data.child('numOfInterestedMembers').val(),
                numberOfParticipants: raw_data.child('numberOfParticipants').val(),
                lat: raw_data.child('lat').val(),
                lng: raw_data.child('lng').val(),
                locationName: raw_data.child('locationName').val(),
                locationAddress: raw_data.child('locationAddress').val(),
                commentsNumber: raw_data.child('comments').numChildren(),
                state: raw_data.child('state').val(),
                country: raw_data.child('country').val()
            }
            const score = 2*dataOfEvent.numberOfParticipants + dataOfEvent.numOfInterestedMembers
            dataOfEvent['elementScore'] = score

            data['events'].push(dataOfEvent)
            }

        })
        return admin.database().ref('public').once('value')
    }).then(snapshot => {
        for (var i = 0; i < data.events.length; i++) 
            data.events[i].name = snapshot.child(data.events[i].userPublicKey).child('profile').child('nickname').val()


        if (dataType === "TRADING"){
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
    const eventDetails = request.eventDetails
    const lat = request.lat
    const lng = request.lng
    const locationName = request.locationName
    const locationAddress = request.locationAddress
    const timestamp = Date.now()
    const state = request.state
    const country = request.country

    var data = {
        userPublicKey: account.publicKey,
        createdEventTime: timestamp,
        eventTime: eventTime,
        eventDate: eventDate,
        locationName: locationName,
        locationAddress: locationAddress,
        eventDetails: eventDetails,
        lat: lat,
        lng: lng,
        state: state,
        country: country
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

    const verifyReciever =  await getNickname(receiverPublicKey)

    if (verifyReciever === null){
        return "ERR:NO_USER"
    }

    const data = {
        receiverPublicKey: receiverPublicKey,
        senderPublicKey: senderPublicKey,
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
            childName = "numberOfParticipants"
        else 
            childName = "numOfInterestedMembers"
      const collectionRef = change.after.ref.parent;
      const countRef = collectionRef.parent.child(childName);

    if (!change.after.exists() && change.before.exists() && change.before.val <= 0){
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

    const container = request.container
    const comment = request.comment
    const postId = request.postId
    const replyTo = request.replyTo.trim()

    if (container !== "global_posts" && container !== "events")
        return "WRONG_CONTAINER";

    const data = {
        senderPublicKey: account.publicKey,
        comment: comment,
     //   postId: postId,
        timestamp: Date.now(),
    }

    var messageId;

    if (replyTo !== ""){
        messageId = await admin.database().ref(container).child(postId).child('comments').child(replyTo).push().key
        admin.database().ref(container).child(postId).child('comments').child(replyTo).child('subComments').child(messageId).set(data)
    }
    else{
        messageId = await (await admin.database().ref(container).child(postId).child('comments').push()).key
        admin.database().ref(container).child(postId).child('comments').child(messageId).set(data)
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
    if (a.exists()){
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
        if (a.exists()){
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
                && distanceFromMe(raw_data.child('lat').val(), raw_data.child('lng').val(), lat, lng) <= range){
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

    if (name.length <= 2){
        return "TOO_SHORT"
    }
    
    var array = {}
    array['users'] = []

    return admin.database().ref('nicknames').once('value').then(snapshot => {
        snapshot.forEach(raw_data => {
            if (raw_data.key.toLowerCase().includes(name)){
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

exports.like = functions.database.ref('global_posts/{postId}/likes/{userId}').onWrite(
    async (change) => {
        const collectionRef = change.after.ref.parent;
        const countRef = collectionRef.parent.child('likes_count');
  
        let increment;

        if (change.before.exists() && !change.after.exists() && !collectionRef.parent('timestamp').exists()){
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

exports.commentCountTrigger = functions.database.ref('global_posts/{postId}/comments/{commentId}').onWrite(
    async (change) => {
            const collectionRef = change.after.ref.parent;
            const countRef = collectionRef.parent.child('comments_count');
      
            let increment;
    
            if (change.before.exists() && !change.after.exists() && !collectionRef.parent('timestamp').exists()){
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
                    if (groups.includes(raw_post.key)){
                        data['groups'].push({
                            groupId: raw_post.key,
                            title: raw_post.child('title').val(),
                            description: raw_post.child('description').val()
                        })
                    }
                })
                return null;
            }).then(x=> {
                return JSON.stringify(data)
            })
        })



        exports.GetAllGroups = functions.https.onCall(async (request, context) => {       
            var data = {}
            data['groups'] = []
        
            return admin.database().ref('groups').once('value').then(snapshot => {
                snapshot.forEach(raw_post => {
                    var group = []
                    group = ({
                        groupId: raw_post.key,
                        title: raw_post.child('title'),
                        description: raw_post.child('description')
                    })
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

            await admin.database().ref('public').child(account.publicKey).child('connected_groups').child(groupId).set(true)
            return "OK"
        })

        exports.GetGroupPosts = functions.https.onCall(async (request, context) => {

            const account = await verifyUser(context.auth.uid);
            if (account === null)
                return "AUTH_FAILED"

            const groupId = request.groupId
            
            var data = {}
            data['posts'] = []
        
            return admin.database().ref('groups').child(groupId).child('posts').once('value').then(snapshot => {
                snapshot.forEach(raw_post => {

                    var dataOfUser = ({
                        postId: raw_post.key,
                        publicKey: raw_post.child('user_public_key').val(),
                        name: "...",
                        message: raw_post.child('message').val(),
                        timestamp: raw_post.child('timestamp').val(),
                    })
        
                    if (raw_post.child('comments_count').exists()){
                        dataOfUser['comments_count'] = 
                             raw_post.child('comments_count').val()
                    }
        
                    if (raw_post.child('likes_count').exists()){
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
 