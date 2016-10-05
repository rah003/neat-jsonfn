[#assign page = cmsfn.page(content)]

[#if !(ctx.getParameter("workspace")?has_content)]
    Use parameter 'workspace' in url to get json of content you want. e.g.: <a href='${ctx.contextPath}${page.@path}.js?workspace=tours'>${ctx.contextPath}${page.@path}.js?workspace=tours</a>
[#else]
    [#assign workspace = ctx.getParameter("workspace")]
    [#assign json = jsonfn.fromChildNodesOf(workspace).addAll().down(5).print()]
    [#--[#assign json = jsonfn.fromChildNodesOf(workspace).add("@link","name").expand("image","dam").down(5).print()]--]
    ${json}
[/#if]
