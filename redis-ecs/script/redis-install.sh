#!/bin/bash

############################################################################
# @desc:
#	- 1. install redis;
#	- 2. @usage: sh redis-install.sh [username] [redisTarGz]
###########################################################################

set -o nounset
set -o errexit

# install redis，Redis6.0及以上版本需要依赖操作系统gcc 4.9.0以上版本编译
installRedis() {
  yum install -y gcc
  local redisDir="/opt/cachecloud/$3"
  local redisTarGz="$3.tar.gz"
  mkdir -p ${redisDir} && cd ${redisDir}
  wget http://download.redis.io/releases/${redisTarGz} && tar zxvf ${redisTarGz} --strip-component=1
  make && make install
  if [[ $? == 0 ]]; then
    echo -e "\033[41;36m OK: ${redisTarGz} is installed, exit. \033[0m"
    chown -R $1:$2 ${redisDir}
    return
  fi
  echo -e "\033[41;36m ERROR: ${redisTarGz} is NOT installed, exit. \033[0m"
}

# main entrance
redisVersion="redis-5.0.9"
username="cachecloud-open"
password="cachecloud-open"
if [[ $# > 0 && -n "$1" ]]; then
  username="$1"
  echo -e "\033[41;36m please input password for user: ${username} \033[0m"
  stty -echo
  read password
  stty echo
fi
echo -e "\033[41;36m use username: ${username} \033[0m"
if [[ $# > 1 && -n "$2" ]]; then
  redisVersion="$2"
  echo -e "\033[41;36m install redisVersion: $2 \033[0m"
fi
installRedis "${username}" "${password}" "${redisVersion}"
