## 2025.09 Matrix Push 기술회의

### android
### ios 

- FCM/APNs 로 부터 메시지를 받아서 상품상세 화면으로 바로 이동 한다.

- iOS 샘플
```json
{
  "title": "string",
  "google.c.fid": "fHEkOzCWMEK3p-Ivz2af8u",
  "color": "#FFFFFF",
  "channelId": "matrix_push_channel",
  "messagePriority": "NORMAL",
  "payload": {
    "productId": "p2"
  },
  "aps": {
    "alert": {
      "title": "string",
      "launch-image": "https://image.example.com/image0308",
      "body": "string"
    },
    "mutable-content": 1
  },
  "messageId": "30",
  "campaignId": "22",
  "sender": "fb066b81-27b3-44e3-8a61-1072400dab81",
  "pushDispatchId": "307e3e63-a036-1f99-0346-0346046f9253",
  "google.c.a.e": "1",
  "body": "string",
  "channelDescription": "Matrix Push SDK에서 발송하는 알림",
  "channelName": "Matrix Push 알림",
  "messageType": "NOTIFICATION",
  "google.c.sender.id": "984176238269",
  "imageUrl": "https://image.example.com/image0308",
  "fcm_options": {
    "image": "https://image.example.com/image0308"
  },
  "icon": "matrix_push_icon",
  "timestamp": "2025-09-19T16:35:54",
  "gcm.message_id": "1758267354517446"
}
```