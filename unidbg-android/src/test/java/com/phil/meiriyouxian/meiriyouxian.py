def b64encode(text):
    bins = str()
    for c in text:
        bins += '{:0>8}'.format(str(bin(c))[2:])
    while len(bins) % 3:
        bins += '00000000'
    for i in range(6, len(bins) + int(len(bins) / 6), 7):
        bins = bins[:i] + ' ' + bins[i:]
    bins = bins.split(' ')
    if '' in bins:
        bins.remove('')
    base64 = str()
    for b in bins:
        if b == '000000':
            base64 += '='
        else:
            base64 += 'abcdefghijklmnopqrstuvwxyz+ZYXWVUTSRQPONMLKJIHGFEDCBA/1234567890'[
                int(b, 2)]
    return base64


print(b64encode(bytes.fromhex("3136323637393535307ffcf9fd838e7e9bb7bbb0c3beb1b0b7abb2a9b3beb7c3b3bdbbaec3c0bfb1c3b0b3bcbfbfb2adbaa8b2b2b2adaeb2b9aeb2b2bfbfb1b6b8aeacb1b2c0bfafb7bdaea9b4beb1c0c4bcaa7a36383639")).replace("=", ""))
