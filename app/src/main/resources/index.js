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

obj.log(`[global] before data const: `);
const data = `
{
    "iss": "https://access.line.me",
    "sub": "U6339db851f0dd06878589cb0e7008294",
    "aud": "1656158895",
    "exp": 1625047165,
    "iat": 1625043565,
    "name": "Ryo.K",
    "picture": "https://profile.line-scdn.net/0hrql_SQISLV5RJjv-RrVSCW1jIzMmCCsWKUBja3UhIzspEmkMbUZlOHcgczsoEG8AbkBrbSQucWp9"
}
`;

obj.log(`[global] after  data const: ${data}`);
obj.log(`[global] before function initializeLiff`);

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

const code = urlParams.get("code")
if (code) {
    obj.log(`[ifcode_] code: ${code}`)
    fetch("/fetch", {
        method: "POST",
        headers: {
            "Content-Type": 'application/json'
        },
        body: data
    }).then(e => {
        const js = e.json()
        obj.log(`[fetch] js log: ${js}`)
    })
} else {
    obj.log(`[ifcode_] no code.. 🥺`)
    initializeLiff(liffId);
    obj.log(`[global] before initializeLiff`);
    obj.log(`[global] after  initializeLiff`);
}
