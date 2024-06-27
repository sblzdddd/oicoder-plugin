package com.github.sblzdddd.oicoderplugin

class Constants {
    companion object {
        private const val USERNAME = "lizidong@bishen.com"
        private const val PASSWORD = "aA123456"
        const val HOME_URL = "http://oi.beecent.cn/"
        const val LOGIN_URL = "http://oi.beecent.cn/login/index.php"
        const val QUIZ_URL = "http://oi.beecent.cn/mod/quiz/attempt.php"
        const val SUMMARY_URL = "http://oi.beecent.cn/mod/quiz/summary.php"
        const val COURSE_URL = "http://oi.beecent.cn/course/view.php"

        val COMMON_JS = """
            
        """.trimIndent()

        val HOME_JS = """
            
            // 检测是否登录，自动跳转登录
            let element = document.querySelector('div.usermenu > span.login');
            if (typeof(element) != 'undefined' && element != null)
            {
                console.log("Automatically redirecting to login page");
                // 跳转登录
                window.location.href = "$LOGIN_URL";
            } else {
                console.log("User already logged in!");
            }
        """.trimIndent();

        val LOGIN_JS = """
            
            // 自动输入用户名，密码
            document.querySelector("input#username").value = "$USERNAME";
            document.querySelector("input#password").value = "$PASSWORD";
            
            // 提交
            // setTimeout(function(){
            document.querySelector("button#loginbtn").click();
            // }, 200);
                
        """.trimIndent();

        val COURSE_JS = """
            // 隐藏额外课程内容，保留题目
            const video_icon = "http://oi.beecent.cn/theme/image.php/boost/url/1712654822/icon";
            const video_text = "----题解视频----";
            $("div[role='main']").prepend($(`
              <div>
                <input type="checkbox" id="hideGeneral" name="hideGeneral" checked />
                <label for="hideGeneral">Hide General</label>
              </div>
              <div>
                <input type="checkbox" id="hideVideo" name="hideVideo" checked />
                <label for="hideVideo">Hide Video</label>
              </div>
            `));
            ${'$'}("li.activity.label.modtype_label").hide();
            ${'$'}("li.activity.url.modtype_url").hide();
            ${'$'}("li[aria-label='常规']").hide();
            $('#hideGeneral').change(function() {
                if ($(this).is(':checked')) {
                    $("li[aria-label='常规']").hide();
                } else {
                    $("li[aria-label='常规']").show();
                }
            })
            $('#hideVideo').change(function() {
                if ($(this).is(':checked')) {
                    $("li.activity.label.modtype_label").hide();
                    $("li.activity.url.modtype_url").hide();
                } else {
                    $("li.activity.label.modtype_label").show();
                    $("li.activity.url.modtype_url").show();
                
                }
            })
        """.trimIndent()

        val QUIZ_JS = """
            // 题目代码编辑器换成 Monaco Editor （VSCode）
            console.log("init quiz")
            $(".ui_wrapper").empty();
            require.config({ paths: { 'vs': 'https://unpkg.com/monaco-editor@0.8.3/min/vs' }});
            window.MonacoEnvironment = { getWorkerUrl: () => proxy };

            let proxy = URL.createObjectURL(new Blob([`
            	self.MonacoEnvironment = {
            		baseUrl: 'https://unpkg.com/monaco-editor@0.8.3/min/'
            	};
            	importScripts('https://unpkg.com/monaco-editor@0.8.3/min/vs/base/worker/workerMain.js');
            `], { type: 'text/javascript' }));

            require(["vs/editor/editor.main"], function () {
            	let editor = monaco.editor.create(document.querySelector('.ui_wrapper'), {
            		value: $("textarea.coderunner-answer").text(),
            		language: 'cpp',
            		theme: 'vs-dark',
                    automaticLayout: true,
            	});
            	
            	editor.getModel().onDidChangeContent((event) => {
                    $("textarea.coderunner-answer").val(editor.getValue());
                });
                
                window.receiveCode = (code) => {
                    console.log('Received code from plugin: \n', code);
                    editor.getModel().setValue(code);
                    $("textarea.coderunner-answer").val(editor.getValue());
                }
            });
            $("div.formulation").append($(`
                <div style="display:flex; margin-top:15px; gap:10px">
                    <button class="cl-btn" type="button" id="cl-create">Create File in CLion</button>
                    <button class="cl-btn" type="button" id="cl-paste">Paste from CLion</button>
                </div>
            `));
            $("button#cl-paste").on("click", function(){
                handlePasteButtonClick();
            })
            $("button#cl-create").on("click", function(){
                handleCreateButtonClick();
            })
            function handlePasteButtonClick() {
                if (window.getCode) {
                    window.getCode();
                } else {
                    console.error('getCode() is not available.');
                }
            }
            function handleCreateButtonClick() {
                if (window.createCppFile) {
                    const fileName = $("li.breadcrumb-item").last().find("a").text()
                    window.createCppFile(fileName);
                } else {
                    console.error('createCppFile() is not available.');
                }
            }
        """.trimIndent()

        val SUBMIT_JS = """
            // 自动提交答案
            $("button:contains('提交所有答案并结束')").click();
            $("input[value='提交所有答案并结束']").click();
        """.trimIndent()

        val CPP_CONTENT = """// Created using OICoder-Plugin
#include <bits/stdc++.h>
using namespace std;

#define l long long;

signed main() {
    return 0;
}

"""
    }
}
