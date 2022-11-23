import hashlib

s1 = '12345'
s2 = 'r0ysue'
sign = hashlib.md5((s1 + s2).encode(encoding='UTF-8')).hexdigest()
print(f"{sign[1]}{sign[5]}{sign[2]}{sign[10]}{sign[17]}{sign[9]}{sign[25]}{sign[27]}")
