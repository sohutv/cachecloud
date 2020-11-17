# create public key & secret key
createSshkey() {
  mkdir -p /opt/ssh
  chown -R $1:$2 /opt/ssh

  ssh-keygen -t rsa -f /opt/ssh/id_rsa -P '' -C $1
  local privateKeyFile="/opt/ssh/id_rsa"
  chmod 600 ${privateKeyFile}

  mkdir -p /home/$1/.ssh
  local publicKeyFile="/home/$1/.ssh/authorized_keys"
  cat /opt/ssh/id_rsa.pub >>${publicKeyFile}
  chown -R $1:$2 /home/$1/.ssh
  chmod 755 ${publicKeyFile}

  echo -e "\033[41;36m OK: create public key & secret key done. \033[0m"
}



# let's go
username="cachecloud-open"
password="cachecloud-open"
if [[ $# > 0 && -n "$1" ]]; then
  username="$1"
  echo -e "\033[41;36m please set password for user: ${username} \033[0m"
  stty -echo
  read password
  stty echo
fi
echo -e "\033[41;36m use username: ${username}. \033[0m"
# create public key & secret key
createSshkey "${username}" "${password}"