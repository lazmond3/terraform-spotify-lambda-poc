const decodeJwt = (token) => {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(decodeURIComponent(escape(window.atob(base64))));
};

const obj = {
    textarea: document.getElementById("t"),
    log(newText) {
        this._text = newText + "\n" + this._text;
        this.textarea.textContent = this._text;
    },
    _text: ""
}

obj.log("hello world")
const liffId = "1656158895-Rygo23DL"

// obj.log(`[global] before data const: `);
// obj.log(`[global] after  data const: ${data}`);
// obj.log(`[global] before function initializeLiff`);

function initializeLiff(myLiffId) {
    liff
        .init({
            liffId: myLiffId
        })
        .then(() => {
            // start to use LIFF's api
            obj.log(`[liff init]`)
            const idTokenJwt = liff.getIDToken();
            const decoded = decodeJwt(idTokenJwt);
            obj.log(`[liff init] decoded: ${decoded}`)
            const idToken = liff.getDecodedIDToken();
            document.getElementById("decoded").textContent = JSON.stringify(idToken);


            document.getElementById("uid").textContent = idToken.sub
            document.getElementById("url").textContent = location.href;

            // 1. init に成功して、データを飛ばして mode to reg にできたら、spotify にリダイレクトする。
            // [fetch API]

        })
        .catch((err) => {
            obj.log(`失敗しました: ${err}`)
        });
}
// 2. もし code が設定されてたら、 initializeLiff を 実行せず、 code を表示するだけ。
// if (false) {

const queryString = window.location.search;
obj.log(`[queryString] : ${queryString}`);
const urlParams = new URLSearchParams(queryString);

async function postDataString(url = '', data = {}) {
    // 既定のオプションには * が付いています
    const response = await fetch(url, {
        method: 'POST', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json'
            // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        body: (data) // 本文のデータ型は "Content-Type" ヘッダーと一致する必要があります
    })
    return response.json(); // レスポンスの JSON を解析
}

async function postData(url = '', data = {}) {
    // 既定のオプションには * が付いています
    const response = await fetch(url, {
        method: 'POST', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json'
            // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        body: JSON.stringify(data) // 本文のデータ型は "Content-Type" ヘッダーと一致する必要があります
    })
    return response.json(); // レスポンスの JSON を解析
}


const code = urlParams.get("code")
if (code) {
    obj.log(`[ifcode_] code: ${code}`)
    const data = `
        {
            "iss": "https://access.line.me",
            "sub": "U6339db851f0dd06878589cb0e7008294",
            "aud": "1656158895",
            "exp": 1625047165,
            "iat": 1625043565,
            "code": "${code}"
            "name": "Ryo.K",
            "picture": "https://profile.line-scdn.net/0hrql_SQISLV5RJjv-RrVSCW1jIzMmCCsWKUBja3UhIzspEmkMbUZlOHcgczsoEG8AbkBrbSQucWp9"
        }
`;
    // (async () => {
    //     const result = await postDataString('/post', data);
    //     obj.log(`result in postDataString: ${result}`)
    // })();

    // .then(data => {
    // console.log(data); // `data.json()` の呼び出しで解釈された JSON データ
    // });
    fetch("/test/post", {
        method: "POST",
        headers: {
            "Content-Type": 'application/json'
        },
        body: data
    }).then(e => {
        const js = e.json()
        obj.log(`[fetch] js log: ${JSON.stringify(js)}`)
    }).catch(e => {
        const ej = JSON.stringify(e);
        obj.log(`[fetch error] e: ${e}, ej: ${ej}`);
    })
} else {
    obj.log(`[ifcode_] no code.. 🥺`)
    initializeLiff(liffId);
    obj.log(`[global] before initializeLiff`);
    obj.log(`[global] after  initializeLiff`);
}
