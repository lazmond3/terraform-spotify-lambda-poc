package terraform.spotify.lambda.poc

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Index
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.NameMap
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import com.linecorp.bot.model.message.quickreply.QuickReply.items
import terraform.spotify.lambda.poc.entity.PlaylistTrack



fun main() {
    println("hello world")
    val client = AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.AP_NORTHEAST_1)
        .build()

    val tableName = "spotify-dynamo-music"
    val playlistTrack = PlaylistTrack(
        userId = "testUserId",
        playlistId = "playlistId",
        trackId = "trackId"
    )


    client.putItem(
        tableName, mapOf(
            "UserId" to AttributeValue(playlistTrack.userId),
            "PlaylistId" to AttributeValue(playlistTrack.playlistId),
            "TrackId" to AttributeValue(playlistTrack.trackId)
        )
    )

    val indexName = "SpotifyDynamoMusicPlaylistIndex"
//    val spec = QuerySpec()
//        .withKeyConditionExpression("#PlaylistId = :playlistId")
//        .withNameMap(
//            NameMap()
//                .with("#d", "UserId")
//        )
//        .withValueMap(
//            ValueMap()
//                .withString(":playlistId", playlistTrack.playlistId)
//                .withString(":userId", playlistTrack.userId)
////                .withNumber(":v_precip", 0)
//        )

    val dynamoDB = DynamoDB(client)
    val table = dynamoDB.getTable(tableName)
    val index: Index = table.getIndex(indexName)
//    val items = index.query(spec)

//    val iter: Iterator<Item> = items.iterator()
//    while (iter.hasNext()) {
//        println(iter.next().toJSONPretty())
//    }


    val itemCollection = client.query(
        QueryRequest()
            .withTableName(tableName)
//            .withIndexName(indexName)
            .withKeyConditionExpression("PlaylistId = :playlistId and TrackId = :trackId")
            .withExpressionAttributeValues(
                mapOf(
                    ":playlistId" to AttributeValue(playlistTrack.playlistId),
                    ":trackId" to AttributeValue(playlistTrack.trackId)
                )
            )
//            .withKeyConditions(
//                mapOf(
////                    "UserId" to Condition()
////                        .withComparisonOperator(ComparisonOperator.EQ)
////                        .withAttributeValueList(AttributeValue().withS(playlistTrack.userId)),
//                    "PlaylistId" to Condition()
//                        .withComparisonOperator(ComparisonOperator.EQ)
//                        .withAttributeValueList(AttributeValue().withS(playlistTrack.playlistId)),
//                    "TrackId" to Condition()
//                        .withComparisonOperator(ComparisonOperator.EQ)
//                        .withAttributeValueList(AttributeValue().withS(playlistTrack.trackId)),
//                )
//            )
//            .addQueryFilterEntry()
//            .withExpressionAttributeValues(
//                mapOf(
//                    ":userId" to AttributeValue(playlistTrack.userId),
//                    ":playlistId" to AttributeValue(playlistTrack.playlistId),
//                )
//            )
//            .withFilterExpression("TrackId = :trackId")
//            .withExpressionAttributeValues(
//                mapOf(
//                    ":trackId" to AttributeValue(playlistTrack.trackId)
//                )
//            )
//            .withFilterExpression("PlaylistId = :playlistId")
//            .withExpressionAttributeValues(
//                mapOf(
//                    ":playlistId" to AttributeValue(playlistTrack.playlistId)
//                )
//            )
//            .withFilterExpression("TrackId = #trackId")
//            .withExpressionAttributeValues(
//                mapOf(
//                    "#trackId" to AttributeValue(playlistTrack.trackId)
//                )
//            )
    )
    for (i in itemCollection.items) {
        println("i: $i")
    }
}
