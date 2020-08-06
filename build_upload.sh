#!/bin/sh
# author hardyshi

#函数定义，检测执行结果
function checkResult() {
  result=$?
  echo "result : $result"
  if [ $result -eq 0 ]; then
    echo "checkResult: execCommand succ"
  else
    echo "checkResult: execCommand failed"
    exit $result
  fi
}

deleteempty() {
  find ${1:-.} -mindepth 1 -maxdepth 1 -type d | while read -r dir; do
    if [[ -z "$(find "$dir" -mindepth 1 -type f)" ]] >/dev/null; then
      echo "$dir"
      rm -rf ${dir} 2>&- && echo "Empty, Deleted!" || echo "Delete error"
    fi
    if [ -d ${dir} ]; then
      deleteempty "$dir"
    fi
  done
}
echo "********APK build init set env *******"
localPath=$(pwd)
echo "localPath:"$localPath
hasNotCommit=$(git status | grep "what will be committed" | wc -l)
if [ $hasNotCommit -gt 0 ]; then
  echo "------------- git has code not commit !!!!!!!!!!!! -------------"
   exit
fi
libName=$1
version=$2
echo "libName:"$libName
echo "version:"$version
if [ "$libName"x = ""x ]; then
  echo "------------- libName can not be null !!!!!!!!!!!! -------------"
  exit
fi

if [ "$version"x = ""x ]; then
  echo "------------- version can not be null !!!!!!!!!!!! -------------"
  exit
fi
echo "******** build mkdir bin *******"
cd $localPath
#临时文件
if [ ! -d "$localPath/bin" ]; then
  mkdir "$localPath/bin"
fi
rm -fr $localPath/bin/*

# 修改版本名称
src="def *mainProject *= *\\\""
dst="def mainProject = \\\"${libName}\\\""
cat $localPath/build_module.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/build_module.gradle
mv -f $localPath/bin/build_module.gradle $localPath/build_module.gradle
src="def *developModule *= *\\\""
dst="def developModule = \\\"${libName}\\\""
cat $localPath/build_module.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/build_module.gradle
mv -f $localPath/bin/build_module.gradle $localPath/build_module.gradle

src="[0-9]*\.[0-9]*\.[0-9]*"
cat $localPath/build_module.gradle | sed "/ *\\\"${libName}\\\" *: */,/version/s/${src}/${version}/"  >$localPath/bin/build_module.gradle
mv -f $localPath/bin/build_module.gradle $localPath/build_module.gradle
if [[ $libName == Router* ]]; then
  ./gradlew clean bintrayUpload -PdryRun=false
else
  ./gradlew clean bintrayUpload -PdryRun=false -xjavadocRelease -xlint
fi
checkResult
git add $localPath/build_module.gradle
git commit $localPath/build_module.gradle -m"auto add by build.sh, author:zixie "
checkResult
