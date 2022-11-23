import hashlib

salt = "YP1Vty&$Xm*kJkoR,Opk&"
data = salt + 'aid=01A-khBWIm48A079Pz_DMW6PyZR8uyTumcCNm4e8awxyC2ANU.&cfrom=28B5295010&cuid=5999578300&noncestr=46274W9279Hr1X49A5X058z7ZVz024&platform=ANDROID&timestamp=1621437643609&ua=Xiaomi-MIX2S__oasis__3.5.8__Android__Android10&version=3.5.8&vid=1019013594003&wm=20004_90024'
print(hashlib.md5(data.encode(encoding='UTF-8')).hexdigest())
