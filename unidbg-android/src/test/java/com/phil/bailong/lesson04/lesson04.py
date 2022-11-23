import base64
import hashlib
import hmac


class SHA1:
    def __init__(self):
        # 改魔数
        self.__H = [
            0x67452301,
            0xEFCDAB89,
            0x98BADCFE,
            0x5E4A1F7C,
            0x10325476
        ]

    def __str__(self):
        return ''.join((hex(h)[2:]).rjust(8, '0') for h in self.__H)

    # Private static methods used for internal operations.
    @staticmethod
    def __ROTL(n, x, w=32):
        return ((x << n) | (x >> w - n))

    @staticmethod
    def __padding(stream):
        l = len(stream)  # Bytes
        hl = [int((hex(l*8)[2:]).rjust(16, '0')[i:i+2], 16)
              for i in range(0, 16, 2)]

        l0 = (56 - l) % 64
        if not l0:
            l0 = 64

        if isinstance(stream, str):
            stream += chr(0b10000000)
            stream += chr(0)*(l0-1)
            for a in hl:
                stream += chr(a)
        elif isinstance(stream, bytes):
            stream += bytes([0b10000000])
            stream += bytes(l0-1)
            stream += bytes(hl)

        return stream

    @staticmethod
    def __prepare(stream):
        M = []
        n_blocks = len(stream) // 64

        stream = bytearray(stream)

        for i in range(n_blocks):  # 64 Bytes per Block
            m = []

            for j in range(16):  # 16 Words per Block
                n = 0
                for k in range(4):  # 4 Bytes per Word
                    n <<= 8
                    n += stream[i*64 + j*4 + k]

                m.append(n)

            M.append(m[:])

        return M

    # Private instance methods used for internal operations.

    def __process_block(self, block):
        MASK = 2**32-1

        W = block[:]
        for t in range(16, 80):
            W.append(SHA1.__ROTL(1, (W[t-3] ^ W[t-8] ^ W[t-14] ^ W[t-16]))
                     & MASK)

        a, b, c, d, e = self.__H[:]

        # 4轮变5轮
        for t in range(80):
            if t <= 15:
                K = 0x5a827999
                f = (b & c) ^ (~b & d)
            elif t <= 19:
                K = 0x6ed9eba1
                f = b ^ c ^ d
            elif t <= 39:
                K = 0x8f1bbcdc
                f = (b & c) ^ (b & d) ^ (c & d)
            elif t <= 59:
                K = 0x5a827999
                f = (b & c) ^ (~b & d)
            else:
                K = 0xca62c1d6
                f = b ^ c ^ d

            T = ((SHA1.__ROTL(5, a) + f + e + K + W[t]) & MASK)
            e = d
            d = c
            c = SHA1.__ROTL(30, b) & MASK
            b = a
            a = T

        self.__H[0] = (a + self.__H[0]) & MASK
        self.__H[1] = (b + self.__H[1]) & MASK
        self.__H[2] = (c + self.__H[2]) & MASK
        self.__H[3] = (d + self.__H[3]) & MASK
        self.__H[4] = (e + self.__H[4]) & MASK

    # Public methods for class use.
    def update(self, stream):
        stream = SHA1.__padding(stream)
        stream = SHA1.__prepare(stream)

        for block in stream:
            self.__process_block(block)

    def digest(self):
        pass

    def hexdigest(self):
        s = ''
        for h in self.__H:
            s += (hex(h)[2:]).rjust(8, '0')
        return s


def main():
    h = SHA1()
    h.update(b"r0ysue")
    hex_sha = h.hexdigest()
    print(hex_sha)

    key = b'9c2d2df9a049d1c489d084ca535daada&b51c30e9ec2b3beb549cbb2f6e766abd'
    s = b'input1'
    digest = hmac.new(key, s, hashlib.sha1).digest()
    s0 = base64.b64encode(digest).decode()
    print(s0)


if __name__ == '__main__':
    main()
