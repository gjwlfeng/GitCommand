<#list logInfos as logInfo>
### ${logInfo.versionName}
<#list logInfo.logs as log>
${log}
</#list>

###### 额外信息
<#assign extraKeys = logInfo.extraMap?keys>
<#list extraKeys as key>
${key} = ${logInfo.extraMap[key]}; 
</#list>

</#list>