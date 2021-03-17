#!/bin/bash

############################################################################
# @desc:
# 1. initial config
# 2. add system config
#	3. create user;
#	4. create default directories and authorize;
# 5. install sshpass tool
# 6. install redis
#	@usage: sh cachecloud-init.sh [username]
###########################################################################

set -o nounset
set -o errexit

# Redis6.0及以上版本需要依赖操作系统gcc 4.9.0以上版本编译
redis_array=("redis-3.0.7" "redis-3.2.12" "redis-4.0.14" "redis-5.0.9")

# initial config
initConfig() {
  sysctl vm.overcommit_memory=1
  echo 511 >/proc/sys/net/core/somaxconn
  echo never >/sys/kernel/mm/transparent_hugepage/enabled
  echo never >/sys/kernel/mm/transparent_hugepage/defrag
  echo 0 >/proc/sys/vm/swappiness &
  nohup swapoff -a >swap.out 2>&1
  echo -e "\033[41;36m OK: initial config done. \033[0m"
}

# add system config
initSysConfig() {
  echo "vm.overcommit_memory = 1" >>/etc/sysctl.conf
  echo 'vm.swappiness=0' >>/etc/sysctl.conf
  echo "echo 511 > /proc/sys/net/core/somaxconn" >>/etc/rc.d/rc.local
  echo "echo never >  /sys/kernel/mm/transparent_hugepage/enabled" >>/etc/rc.d/rc.local
  echo "echo never >  /sys/kernel/mm/transparent_hugepage/defrag" >>/etc/rc.d/rc.local
  echo -e "\033[41;36m OK: add system config done. \033[0m"
}

# check if the user exists
checkExist() {
  local num=$(cat /etc/passwd | grep -w $1 | wc -l)

  #cat /etc/passwd | grep -q "$1"
  if [[ $num == 1 ]]; then
    echo -e "\033[41;36m user $1 exists, overwrite user and init all data*: [y/n]? \033[0m"
    read replace
    if [[ ${replace} == "y" ]]; then
      echo -e "\033[41;36m delete existed user: $1. \033[0m"
      userdel -r "$1"
      createUser "$1" "$2"
      init "$1"
      return 0
    fi
  else
    createUser "$1" "$2"
    init "$1"
  fi
  return 0
}

# create the user
createUser() {
  # create a user
  groupadd -g 400 $1
  useradd -u 400 -g 400 -m -d /home/$1 -s /bin/bash $1

  # give the user a password
  echo $2 | passwd --stdin $1

  # add the user to sudoers
  #   echo "$1   ALL=(ALL)   ALL" >> /etc/sudoers

  #  Maximum number of days between password change
  chage -M 9999 $1
  echo -e "\033[41;36m OK: create user: $1 done. \033[0m"
}

# create defautl dirs and authorize
init() {
  # create working dirs and a tmp dir
  mkdir -p /opt/cachecloud/conf
  mkdir -p /opt/cachecloud/data
  mkdir -p /opt/cachecloud/logs
  mkdir -p /tmp/cachecloud
  mkdir -p /data/redis

  # change owner
  chown -R $1:$1 /opt/cachecloud
  chown -R $1:$1 /tmp/cachecloud
  chown -R $1:$1 /home/$1
  chown -R $1 /var/run
  chown -R $1:$1 /data/redis
  echo -e "\033[41;36m OK: init done. \033[0m"
}

# install sshpass tool
installSshpass() {
  yum install -y sshpass
  echo -e "\033[41;36m OK: install sshpass tool done. \033[0m"
}

# output
output() {
  echo "somaxconn="
  cat /proc/sys/net/core/somaxconn
  echo "overcommit_memory="
  cat /proc/sys/vm/overcommit_memory
  echo "transparent_hugepage/enabled="
  cat /sys/kernel/mm/transparent_hugepage/enabled
  echo "transparent_hugepage/defrag="
  cat /sys/kernel/mm/transparent_hugepage/defrag
  echo "swappiness="
  cat /proc/sys/vm/swappiness
  echo "user="
  cat /etc/passwd | grep cachecloud
  echo "auth_key="
  cat /home/$1/.ssh/authorized_keys
  echo "sshpass="
  sshpass -V | head -1

  echo -e "\033[41;36m OK: config output done. \033[0m"
}

# install redis
installRedis() {
  yum install -y gcc
  for redisVersion in ${redis_array[*]}; do
    local redisDir="/opt/cachecloud/${redisVersion}"
    local redisTarGz="${redisVersion}.tar.gz"
    mkdir -p ${redisDir} && cd ${redisDir}
    wget http://download.redis.io/releases/${redisTarGz} && tar zxvf ${redisTarGz} --strip-component=1
    make && make install

    if [[ $? == 0 ]]; then
      echo -e "\033[41;36m OK: ${redisTarGz} is installed, exit. \033[0m"
      chown -R $1:$2 ${redisDir}
      if [[ ${redisVersion} == "redis-5.0.9" ]]; then
        export PATH=$PATH:${redisDir}/src
        echo $PATH
      fi
    else
      echo -e "\033[41;36m ERROR: ${redisTarGz} is NOT installed, exit. \033[0m"
    fi
  done
  return
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
# 1. initial config
initConfig
# 2. add system config
initSysConfig
# 3. check & create user
checkExist "${username}" "${password}"
# 4.install sshpass tool
installSshpass
# 5.output
output "${username}"
# 6.install install
installRedis "${username}" "${password}"
