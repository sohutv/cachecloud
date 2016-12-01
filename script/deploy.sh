#!/bin/bash

cachecloud_dir=$1
base_dir=/opt/cachecloud-web
mkdir -p ${base_dir}
mkdir -p ${base_dir}/logs
cp ${cachecloud_dir}/cachecloud/script/start.sh ${base_dir}
cp ${cachecloud_dir}/cachecloud/script/stop.sh ${base_dir}
cp ${cachecloud_dir}/cachecloud/cachecloud-open-web/target/cachecloud-open-web-1.0-SNAPSHOT.war ${base_dir}
cp ${cachecloud_dir}/cachecloud/cachecloud-open-web/src/main/resources/cachecloud-web.conf  ${base_dir}/cachecloud-open-web-1.0-SNAPSHOT.conf
