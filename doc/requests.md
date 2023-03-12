Messages sent and received at user 1 session:
```
-> {"type":"createRoom", "user":{ "id":"1", "name":"User1" } }
<- {"type":"PlayerHasCreatedRoomEvent","user":{"id":"1","name":"User1","characterId":1720075771},"room":{"id":"835326d5","userIds":["1"]}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"2","name":"User2","characterId":1254224990},"room":{"id":"835326d5","userIds":["1","2"]}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":1861950925},"room":{"id":"835326d5","userIds":["1","2","3"]}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"4","name":"User4","characterId":1938240266},"room":{"id":"835326d5","userIds":["1","2","3","4"]}}
<- {"type":"PlayerHasLeavedRoomEvent","user":{"id":"3"},"room":{"id":"835326d5"}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":-86300885},"room":{"id":"835326d5","userIds":["1","2","4","3"]}}
-> {"type":"roomMessage", "room":{ "id":"835326d5" }, "payload":{ "message":"Hi all!" } }
<- {"userId":"1","roomId":"835326d5","message":"Hi all!"}
```

Messages sent and received at user 2 session:
```
-> {"type":"joinRoom", "user":{ "id":"2", "name":"User2" }, "room":{ "id":"835326d5" } }
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"2","name":"User2","characterId":1254224990},"room":{"id":"835326d5","userIds":["1","2"]}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":1861950925},"room":{"id":"835326d5","userIds":["1","2","3"]}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"4","name":"User4","characterId":1938240266},"room":{"id":"835326d5","userIds":["1","2","3","4"]}}
<- {"type":"PlayerHasLeavedRoomEvent","user":{"id":"3"},"room":{"id":"835326d5"}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":-86300885},"room":{"id":"835326d5","userIds":["1","2","4","3"]}}
<- {"userId":"1","roomId":"835326d5","message":"Hi all!"}
```

Messages sent and received at user 3 session:
```
-> {"type":"joinRoom", "user":{ "id":"3", "name":"User3" }, "room":{ "id":"835326d5" } }
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":1861950925},"room":{"id":"835326d5","userIds":["1","2","3"]}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"4","name":"User4","characterId":1938240266},"room":{"id":"835326d5","userIds":["1","2","3","4"]}}
<- # Closes and recreates connection
-> {"type":"joinRoom", "user":{ "id":"3", "name":"User3" }, "room":{ "id":"835326d5" } }
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":-86300885},"room":{"id":"835326d5","userIds":["1","2","4","3"]}}
<- {"userId":"x","roomId":"835326d5","message":"Hi!"}
```

Messages sent and received at user 4 session:
```
-> {"type":"joinRoom", "user":{ "id":"4", "name":"User4" }, "room":{ "id":"835326d5" } }
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"4","name":"User4","characterId":1938240266},"room":{"id":"835326d5","userIds":["1","2","3","4"]}}
<- {"type":"PlayerHasLeavedRoomEvent","user":{"id":"3"},"room":{"id":"835326d5"}}
<- {"type":"PlayerHasJoinedRoomEvent","user":{"id":"3","name":"User3","characterId":-86300885},"room":{"id":"835326d5","userIds":["1","2","4","3"]}}
<- {"userId":"x","roomId":"835326d5","message":"Hi!"}
```

Messages sent and received at any session:
```
-> {"type":"roomInfo", "room":{ "id":"835326d5" } }
<- {"id":"835326d5","userIds":["1","2","4","3"]}
```