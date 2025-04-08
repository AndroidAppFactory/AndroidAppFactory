#!/bin/sh
# author zixie

# 函数定义，检测执行结果
checkResult() {
  result=$?
  if [ $result -eq 0 ]; then
    echo "checkResult: execCommand succ, libName:$1"
  else
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "checkResult: execCommand failed, libName:$1"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    exit $result
  fi
}

# 处理文件内容：压缩多个空行为一个，并确保最后不是空行
processContent() {
  sed -e '/^[[:space:]]*$/d' "$1" | awk 'NR > 1 && /^[[:space:]]*$/ && p ~ /^[[:space:]]*$/ {next} {p=$0; print}'
}

# 合并字符串资源函数
mergeStrings() {
  local source_dir=$1
  local output_dir=$2
  local output_file="$output_dir/aaf_merged_strings.xml"
  local temp_file=$(mktemp)

  # 创建输出目录
  mkdir -p "$output_dir"
  checkResult "创建目录 $output_dir"

  # 创建 XML 文件头
  echo '<?xml version="1.0" encoding="utf-8"?>' > "$output_file"
  echo '<resources>' >> "$output_file"

  # 临时标记是否是第一个文件
  local first_file=true

  # 遍历并合并文件
  find "$source_dir" -type f -name "string*.xml" | sort | while read -r file; do
    echo "正在处理: $file"

    # 如果不是第一个文件，添加两个空行分隔
    if ! $first_file; then
      echo "" >> "$temp_file"
      echo "" >> "$temp_file"
    else
      first_file=false
    fi

    # 处理文件内容并追加到临时文件
    processContent "$file" | \
    grep -v '<?xml version="1.0" encoding="utf-8"?>' | \
    grep -v '<resources>' | \
    grep -v '</resources>' >> "$temp_file"
  done

  # 处理临时文件内容（压缩多个空行）并追加到输出文件
  processContent "$temp_file" >> "$output_file"
  rm -f "$temp_file"

  # 添加 XML 文件尾
  echo '</resources>' >> "$output_file"
  echo "合并完成！结果保存在: $output_file"
}

localPath=$(pwd)
echo "localPath: $localPath"

# 合并中文资源
mergeStrings "$localPath/ModelRes/src/main/res/values" "$localPath/bin/values"

# 合并英文资源
mergeStrings "$localPath/ModelRes/src/main/res/values-en" "$localPath/bin/values-en"

echo "所有字符串资源合并完成"