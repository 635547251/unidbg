import hashlib

nonce = "A794A7F8-6629-4DE2-852D-4BDB3344EAD4"
timestamp = "1668157201799"
devicetoken = "F1517503-9779-32B7-9C78-F5EF501102BC"
sign = hashlib.md5(f"{nonce}{timestamp}{devicetoken}td9#Kn_p7vUw".encode(encoding='UTF-8')).hexdigest()
# E96725CB0BFE9C3B2E8DCC72B6DCBEF3
print(sign.upper())
