import binascii

_TABLE_RAW = b'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
_TABLE_MISS = b'abcdefghijklmnopqrstuvwxyz+ZYXWVUTSRQPONMLKJIHGFEDCBA/1234567890'
_TRANS = bytes.maketrans(_TABLE_RAW, _TABLE_MISS)
_TRANS_INV = bytes.maketrans(_TABLE_MISS, _TABLE_RAW)


def b64encode(data):
    msg = binascii.b2a_base64(data, newline=False)
    msg = msg.translate(_TRANS)
    msg = msg.decode().rstrip('=')
    return msg


timestamp = 0x17Ac4917cb5
timestamp_a, timestamp_b = str(timestamp)[:-4].encode(), str(timestamp)[-4:].encode()
table_A = bytes.fromhex('088280800810011a40413746433333363438303643394632464135464541344239394342443430393138393532303538373839424433393737323835454132364634303743334343453001')
table_B = timestamp_b
table_c = "ABCDEFGH".encode()
result = bytes([table_A[i] + table_B[i % 4] + table_c[i % 8] for i in range(75)])
print("mfsn" +
      b64encode(timestamp_a + result + timestamp_b))
