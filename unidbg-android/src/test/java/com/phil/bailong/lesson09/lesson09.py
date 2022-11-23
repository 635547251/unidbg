import base64
import hmac
import hashlib


def __clz(x):
    total_bits = 32
    res = 0
    while ((x & (1 << (total_bits - 1))) == 0):
        x = (x << 1)
        res += 1
    return res


def sub_37A0(result, a2):
    v3 = result ^ a2
    if (a2 == 1 and (v3 ^ result) < 0):
        return -result
    else:
        v4 = result
        if (result < 0):
            v4 = -result
        if (v4 <= a2):
            if (v4 < a2):
                result = 0
            if (v4 == a2):
                return (v3 >> 31) | 1
        elif ((a2 & (a2 - 1)) != 0):
            v5 = __clz(a2) - __clz(v4)
            v6 = a2 << v5
            v7 = 1 << v5
            result = 0
            while True:
                if (v4 >= v6):
                    v4 -= v6
                    result |= v7
                if (v4 >= v6 >> 1):
                    v4 -= v6 >> 1
                    result |= v7 >> 1
                if (v4 >= v6 >> 2):
                    v4 -= v6 >> 2
                    result |= v7 >> 2
                if (v4 >= v6 >> 3):
                    v4 -= v6 >> 3
                    result |= v7 >> 3
                v8 = v4 == 0
                if (v4):
                    v7 >>= 4
                    v8 = v7 == 0
                if (v8):
                    break
                v6 >>= 4
            if (v3 < 0):
                return -result
        else:
            result = v4 >> (31 - __clz(a2))
            if (v3 < 0):
                return -result
    return result


def sub_18D4(a1):
    v1 = (2 * a1) ^ 0x1B
    if ((a1 & 0x80) == 0):
        v1 = 2 * a1
    v2 = int(hex(2 * v1)[-2:], 16) ^ 0x1B
    if ((v1 & 0x80) == 0):
        v2 = 2 * v1
    return v2 ^ v1


def sub_18F8(a1):
    v1 = (2 * a1) ^ 0x1B
    if ((a1 & 0x80) == 0):
        v1 = 2 * a1
    v2 = int(hex(2 * v1)[-2:], 16) ^ 0x1B
    if ((v1 & 0x80) == 0):
        v2 = 2 * v1
    return sub_18D4(v2 ^ v1)


def sub_191E(a1):
    v2 = (a1 & 0x80) != 0
    v3 = (2 * a1) ^ 0x1B
    if not v2:
        v3 = 2 * a1
    v4 = v3 ^ a1 ^ sub_18F8(a1)
    return sub_18D4(a1) ^ v4


def sub_194C(a1):
    v1 = a1[0]
    v3 = sub_191E(a1[0])
    v4 = a1[1]
    v24 = v3
    v5 = (2 * v4) ^ 0x1B
    if ((v4 & 0x80) == 0):
        v5 = 2 * v4
    v18 = v5 ^ v4
    v23 = sub_18F8(v4)
    v6 = a1[2]
    v22 = sub_18D4(v6)
    v7 = a1[3]
    v21 = sub_191E(v4)
    v20 = sub_18F8(v6)
    v19 = sub_18D4(v7)
    v8 = v18 ^ sub_18D4(v1)
    v9 = v8 ^ sub_191E(v6)
    v10 = v9 ^ sub_18F8(v7)
    v11 = sub_18F8(v1)
    v12 = sub_18D4(v4)
    v13 = sub_191E(v7)
    a1[2] = v10
    v14 = (2 * v1) ^ 0x1B
    if ((v1 & 0x80) == 0):
        v14 = 2 * v1
    a1[1] = v14 ^ v1 ^ v21 ^ v20 ^ v19
    v15 = (2 * v7) ^ 0x1B
    if ((v7 & 0x80) == 0):
        v15 = 2 * v7
    a1[0] = v15 ^ v24 ^ v23 ^ v22 ^ v7
    v16 = (2 * v6) ^ 0x1B
    if ((v6 & 0x80) == 0):
        v16 = 2 * v6
    a1[3] = v13 ^ v16 ^ v6 ^ v11 ^ v12
    return a1


s = "1622343722"
key = "r0env"
digest = hmac.new(base64.b64encode(key.encode()), bytes.fromhex(
    "00000000" + hex(int(s) + 1)[2:]), hashlib.sha1).digest().hex()
index = int(digest[-2:], 16) & 0xF
v20 = int(digest[index * 2: index * 2 + 8], 16) & 0x7FFFFFFF

res = [0] * 7
for i in range(len(res)):
    v21 = sub_37A0(v20, 26)
    v22 = v20 - 26 * v21
    v20 = v21
    res[i] = "23456789BCDFGHJKMNPQRTVWXY"[v22]
v26 = sum(sub_194C([int(i.encode().hex(), 16) for i in res[1:5]]))
sign = "".join(res)[:-2] + str(v26 - sub_37A0(v26, 0x64) * 0x64)
print(sign)
