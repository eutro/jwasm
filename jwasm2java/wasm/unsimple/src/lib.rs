#[no_mangle]
pub extern fn div(a: i32, b: i32) -> i32 {
    a / b // this is enough to pull in the stdlib apparently
}
